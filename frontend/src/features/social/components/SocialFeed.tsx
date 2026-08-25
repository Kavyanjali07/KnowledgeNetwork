import { AnimatePresence, motion } from "framer-motion";
import { Loader2 } from "lucide-react";
import { useSocialFeed } from "../hooks/useSocialFeed";
import { SocialComposer } from "./SocialComposer";
import { PostCard } from "./PostCard";

export function SocialFeed() {
  const {
    posts,
    isLoading,
    isError,
    refetch,
    isFetchingNextPage,
    workspaceId,
    togglePostLike,
    togglePostFavorite,
    togglePostReaction,
    addComment,
    toggleCommentLike,
    toggleCommentReaction
  } = useSocialFeed();

  const handleToggleLike = (_postId: string, _likedByCurrentUser: boolean) => {
    togglePostLike(_postId);
  };

  return (
    <div className="space-y-4">
      {workspaceId && <SocialComposer workspaceId={workspaceId} />}

      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-2 text-sm text-cyan-300"
          >
            <Loader2 size={18} className="animate-spin" />
            Loading activity...
          </motion.div>
        </div>
      )}

      {isError && (
        <div className="flex items-center justify-center py-12">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex flex-col items-center gap-2 text-sm text-rose-300"
          >
            <span>Failed to load activity feed.</span>
            <button onClick={() => refetch()} className="text-cyan-300 underline">
              Retry
            </button>
          </motion.div>
        </div>
      )}

      {!isLoading && !isError && (
        <AnimatePresence mode="popLayout">
          {posts.map((post, index) => (
            <PostCard
              key={post.id}
              post={post}
              index={index}
              onToggleLike={handleToggleLike}
              onToggleFavorite={togglePostFavorite}
              onToggleReaction={togglePostReaction}
              onAddComment={addComment}
              onToggleCommentLike={toggleCommentLike}
              onToggleCommentReaction={toggleCommentReaction}
            />
          ))}
        </AnimatePresence>
      )}

      {!isLoading && !isError && posts.length === 0 && (
        <div className="flex items-center justify-center py-12 text-sm text-muted-foreground">
          {workspaceId ? "No posts yet. Share your first update!" : "Create a graph workspace before posting activity."}
        </div>
      )}

      {isFetchingNextPage && (
        <div className="flex items-center justify-center py-4">
          <Loader2 size={18} className="animate-spin text-cyan-300" />
        </div>
      )}
    </div>
  );
}
