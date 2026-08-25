import { AnimatePresence, motion } from "framer-motion";
import { ChevronDown, ChevronUp, MessageCircle, Reply } from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { cn } from "../../../lib/cn";
import { socialApi } from "../../../services";
import { useCreateComment } from "../../../hooks/use-mutations";
import { useAuth } from "../../../lib/auth-context";
import type { ReactionEmoji, SocialComment } from "../types";
import { buildCommentTree } from "../utils/comments";
import { formatRelativeTime } from "../utils/time";
import { AnimatedLikeButton } from "./AnimatedLikeButton";
import { MentionInput } from "./MentionInput";
import { MentionText } from "./MentionText";
import { ReactionBar } from "./ReactionBar";
import { UserAvatarLink } from "./UserHoverCard";
import type { SocialCommentResponse } from "../../../services";

type CommentNode = SocialComment & { replies: CommentNode[] };

type CommentItemProps = {
  comment: CommentNode;
  depth: number;
  onReply: (parentId: string, content: string) => void;
  onToggleLike: (commentId: string) => void;
  onToggleReaction: (commentId: string, emoji: ReactionEmoji) => void;
};

function CommentItem({ comment, depth, onReply, onToggleLike, onToggleReaction }: CommentItemProps) {
  const [collapsed, setCollapsed] = useState(false);
  const [replyOpen, setReplyOpen] = useState(false);
  const [replyText, setReplyText] = useState("");
  const hasReplies = comment.replies.length > 0;

  const handleSubmitReply = useCallback(() => {
    const trimmed = replyText.trim();
    if (!trimmed) return;
    onReply(comment.id, trimmed);
    setReplyText("");
    setReplyOpen(false);
    setCollapsed(false);
  }, [comment.id, onReply, replyText]);

  return (
    <motion.div
      layout
      initial={{ opacity: 0, x: -8 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -8 }}
      transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
      className={cn("relative", depth > 0 && "ml-6 border-l border-white/8 pl-4")}
    >
      <div className="group flex gap-3 rounded-lg p-2 transition hover:bg-white/[0.03]">
        <UserAvatarLink userId={comment.authorId} size="sm" />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5">
            <span className="text-sm font-medium">{comment.authorName ?? comment.authorId}</span>
            <span className="text-xs text-muted-foreground">
              @{comment.authorUsername ?? comment.authorId}
            </span>
            <span className="text-xs text-muted-foreground">
              · {formatRelativeTime(comment.createdAt)}
            </span>
          </div>
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
            >
              <p className="mt-1 text-sm leading-6">
                <MentionText content={comment.content} />
              </p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <AnimatedLikeButton
                  size="sm"
                  liked={comment.likedByCurrentUser}
                  count={comment.likes}
                  onToggle={() => onToggleLike(comment.id)}
                />
                <button
                  type="button"
                  onClick={() => setReplyOpen((value) => !value)}
                  className="inline-flex items-center gap-1 rounded-lg px-2 py-1.5 text-xs text-muted-foreground transition hover:bg-white/6 hover:text-foreground"
                >
                  <Reply size={13} />
                  Reply
                </button>
                <ReactionBar
                  compact
                  reactions={comment.reactions}
                  onToggleReaction={(emoji) => onToggleReaction(comment.id, emoji)}
                />
              </div>
              <AnimatePresence>
                {replyOpen && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                    className="mt-3 overflow-hidden"
                  >
                    <MentionInput
                      value={replyText}
                      onChange={setReplyText}
                      placeholder={`Reply to @${comment.authorUsername ?? comment.authorId}...`}
                      rows={2}
                      onSubmit={handleSubmitReply}
                    />
                    <div className="mt-2 flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => setReplyOpen(false)}
                        className="rounded-lg px-3 py-1.5 text-xs text-muted-foreground transition hover:bg-white/6"
                      >
                        Cancel
                      </button>
                      <button
                        type="button"
                        onClick={handleSubmitReply}
                        disabled={!replyText.trim()}
                        className="rounded-lg bg-gradient-to-r from-violet-500 to-cyan-400 px-3 py-1.5 text-xs font-medium text-white transition hover:brightness-110 disabled:opacity-50"
                      >
                        Reply
                      </button>
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          )}
        </div>
        {hasReplies && (
          <button
            type="button"
            onClick={() => setCollapsed((value) => !value)}
            className="flex h-7 shrink-0 items-center gap-1 rounded-md px-2 text-xs text-muted-foreground opacity-0 transition hover:bg-white/6 hover:text-foreground group-hover:opacity-100"
            aria-label={collapsed ? "Expand replies" : "Collapse replies"}
          >
            {collapsed ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
            {comment.replies.length}
          </button>
        )}
      </div>

      <AnimatePresence>
        {!collapsed &&
          comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              depth={depth + 1}
              onReply={onReply}
              onToggleLike={onToggleLike}
              onToggleReaction={onToggleReaction}
            />
          ))}
      </AnimatePresence>
    </motion.div>
  );
}

type CommentThreadProps = {
  postId: string;
  workspaceId: string;
  onAddComment: (postId: string, content: string, parentId?: string) => void;
  onToggleCommentLike: (postId: string, commentId: string) => void;
  onToggleCommentReaction: (postId: string, commentId: string, emoji: ReactionEmoji) => void;
};

function mapCommentResponse(comment: SocialCommentResponse): SocialComment {
  return {
    id: comment.id,
    authorId: comment.authorId,
    authorName:
      comment.author.firstName || comment.author.lastName
        ? `${comment.author.firstName} ${comment.author.lastName}`
        : comment.author.email,
    authorUsername: comment.author.email.split("@")[0] ?? comment.authorId,
    content: comment.content,
    createdAt: comment.createdAt,
    parentId: comment.parentCommentId,
    reactions: [],
    likes: 0,
    likedByCurrentUser: false
  };
}

function flattenComments(comments: SocialCommentResponse[]): SocialComment[] {
  const result: SocialComment[] = [];
  for (const comment of comments) {
    result.push(mapCommentResponse(comment));
    if (comment.replies?.length) {
      result.push(...flattenComments(comment.replies));
    }
  }
  return result;
}

export function CommentThread({
  postId,
  workspaceId,
  onAddComment,
  onToggleCommentLike,
  onToggleCommentReaction
}: CommentThreadProps) {
  const createCommentMutation = useCreateComment(workspaceId);

  const { currentUser } = useAuth();

  const {
    data: commentsResponse,
    isLoading: isLoadingComments
  } = useQuery({
    queryKey: ["social-comments", workspaceId, postId],
    queryFn: () => socialApi.getComments(workspaceId, postId).then((r) => r.data),
    enabled: !!workspaceId && !!postId
  });

  const comments: SocialComment[] = useMemo(() => {
    if (!commentsResponse) return [];
    return flattenComments(commentsResponse.content);
  }, [commentsResponse]);

  const tree = useMemo(() => buildCommentTree(comments) as CommentNode[], [comments]);

  const [newComment, setNewComment] = useState("");
  const [expanded, setExpanded] = useState(true);

  const handleSubmit = useCallback(() => {
    const trimmed = newComment.trim();
    if (!trimmed) return;
    createCommentMutation.mutate(
      { postId, content: trimmed },
      {
        onSuccess: () => {
          setNewComment("");
        }
      }
    );
  }, [newComment, createCommentMutation, postId]);

  const handleReply = useCallback(
    (parentId: string, content: string) => {
      onAddComment(postId, content, parentId);
    },
    [onAddComment, postId]
  );

  return (
    <div className="mt-4 border-t border-white/10 pt-4">
      <button
        type="button"
        onClick={() => setExpanded((value) => !value)}
        className="mb-3 flex items-center gap-2 text-sm font-medium text-muted-foreground transition hover:text-foreground"
      >
        <MessageCircle size={16} />
        {comments.length} {comments.length === 1 ? "comment" : "comments"}
        {expanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
      </button>

      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            {isLoadingComments ? (
              <div className="mb-4 flex items-center gap-2 text-xs text-muted-foreground">
                <span className="animate-pulse">Loading comments...</span>
              </div>
            ) : (
              <>
                <div className="mb-4 flex gap-3">
                  <UserAvatarLink userId={currentUser?.id ?? "unknown"} size="sm" />
                  <div className="flex-1">
                    <MentionInput
                      value={newComment}
                      onChange={setNewComment}
                      onSubmit={handleSubmit}
                      rows={2}
                    />
                    <div className="mt-2 flex justify-end">
                      <button
                        type="button"
                        onClick={handleSubmit}
                        disabled={!newComment.trim() || createCommentMutation.isPending}
                        className="rounded-lg bg-gradient-to-r from-violet-500 to-cyan-400 px-4 py-1.5 text-xs font-medium text-white transition hover:brightness-110 disabled:opacity-50"
                      >
                        Comment
                      </button>
                    </div>
                  </div>
                </div>

                <div className="space-y-1">
            {tree.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                depth={0}
                onReply={handleReply}
                onToggleLike={(commentId) => onToggleCommentLike(postId, commentId)}
                onToggleReaction={(commentId, emoji) => onToggleCommentReaction(postId, commentId, emoji)}
              />
            ))}
                </div>
              </>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}