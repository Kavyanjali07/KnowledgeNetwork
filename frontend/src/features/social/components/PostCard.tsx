import { memo } from "react";
import { motion } from "framer-motion";
import { Bookmark, GitBranch, Share2 } from "lucide-react";
import { useCallback } from "react";
import type { ReactionEmoji, SocialPost } from "../types";
import { formatRelativeTime } from "../utils/time";
import { AnimatedLikeButton } from "./AnimatedLikeButton";
import { CommentThread } from "./CommentThread";
import { MentionText } from "./MentionText";
import { ReactionBar } from "./ReactionBar";
import { UserAvatarLink } from "./UserHoverCard";

type PostCardProps = {
  post: SocialPost;
  index: number;
  onToggleLike: (postId: string, likedByCurrentUser: boolean) => void;
  onToggleFavorite: (postId: string) => void;
  onToggleReaction: (postId: string, emoji: ReactionEmoji) => void;
  onAddComment: (postId: string, content: string, parentId?: string) => void;
  onToggleCommentLike: (postId: string, commentId: string) => void;
  onToggleCommentReaction: (postId: string, commentId: string, emoji: ReactionEmoji) => void;
};

export const PostCard = memo(function PostCard({
  post,
  index,
  onToggleLike,
  onToggleFavorite,
  onToggleReaction,
  onAddComment,
  onToggleCommentLike,
  onToggleCommentReaction
}: PostCardProps) {
  const workspaceId = post.workspaceId;

  const handleAddComment = useCallback(
    (content: string, parentId?: string) => {
      onAddComment(post.id, content, parentId);
    },
    [onAddComment, post.id]
  );

  const handleToggleCommentLike = useCallback(
    (postId: string, commentId: string) => {
      onToggleCommentLike(postId, commentId);
    },
    [onToggleCommentLike]
  );

  const handleToggleCommentReaction = useCallback(
    (postId: string, commentId: string, emoji: ReactionEmoji) => {
      onToggleCommentReaction(postId, commentId, emoji);
    },
    [onToggleCommentReaction]
  );

  return (
    <motion.article
      layout
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      transition={{ duration: 0.3, delay: index * 0.04, ease: [0.16, 1, 0.3, 1] }}
      className="group rounded-2xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl transition hover:border-cyan-300/20"
    >
      <div className="flex gap-3">
        <UserAvatarLink userId={post.authorId} />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5">
            <span className="font-semibold">{post.authorName ?? post.authorId}</span>
            <span className="text-sm text-muted-foreground">
              @{post.authorUsername ?? post.authorId}
            </span>
            <span className="text-sm text-muted-foreground">
              · {formatRelativeTime(post.createdAt)}
            </span>
          </div>

          <div className="mt-2 flex items-center gap-2">
            <span className="inline-flex items-center gap-1 rounded-full border border-white/10 bg-black/16 px-2 py-0.5 text-xs text-cyan-200">
              <GitBranch size={11} />
              {post.entityType}
            </span>
            <span className="text-xs text-muted-foreground">{post.entityName}</span>
          </div>

          <p className="mt-3 text-sm leading-7">
            <MentionText content={post.content} />
          </p>

          <div className="mt-4">
            <ReactionBar
              reactions={post.reactions}
              onToggleReaction={(emoji) => onToggleReaction(post.id, emoji)}
            />
          </div>

          <div className="mt-3 flex items-center gap-1 border-t border-white/8 pt-3">
            <AnimatedLikeButton
              liked={post.likedByCurrentUser}
              count={post.likes}
              onToggle={() => onToggleLike(post.id, post.likedByCurrentUser)}
            />
            <button
              type="button"
              className="inline-flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-sm text-muted-foreground transition hover:bg-white/6 hover:text-foreground"
            >
              <Share2 size={16} />
            </button>
            <button
              type="button"
              onClick={() => onToggleFavorite(post.id)}
              aria-pressed={post.favoritedByCurrentUser}
              aria-label={post.favoritedByCurrentUser ? "Remove favorite" : "Favorite"}
              className="inline-flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-sm text-muted-foreground transition hover:bg-white/6 hover:text-foreground"
            >
              <Bookmark size={16} className={post.favoritedByCurrentUser ? "fill-cyan-300 text-cyan-300" : ""} />
            </button>
          </div>

          <CommentThread
            postId={post.id}
            workspaceId={workspaceId}
            onAddComment={handleAddComment}
            onToggleCommentLike={handleToggleCommentLike}
            onToggleCommentReaction={handleToggleCommentReaction}
          />
        </div>
      </div>
    </motion.article>
  );
});