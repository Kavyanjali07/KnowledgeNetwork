import { motion } from "framer-motion";
import { Send } from "lucide-react";
import { useCallback, useState } from "react";
import { Button } from "../../../components/ui/button";
import { useAuth } from "../../../lib/auth-context";
import { useCreatePost } from "../../../hooks/use-mutations";
import { MentionInput } from "../components/MentionInput";
import { UserAvatarLink } from "../components/UserHoverCard";

type SocialComposerProps = {
  workspaceId: string;
  onSubmit?: (content: string) => void;
};

export function SocialComposer({ workspaceId, onSubmit }: SocialComposerProps) {
  const { currentUser } = useAuth();
  const [content, setContent] = useState("");

  const createPostMutation = useCreatePost(workspaceId);

  const handleSubmit = useCallback(() => {
    const trimmed = content.trim();
    if (!trimmed) return;
    createPostMutation.mutate(
      { content: trimmed, entityType: "Graph", entityId: workspaceId },
      {
        onSuccess: () => {
          setContent("");
          onSubmit?.(trimmed);
        }
      }
    );
  }, [content, createPostMutation, workspaceId, onSubmit]);

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      className="rounded-2xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className="flex gap-3">
        <UserAvatarLink userId={currentUser?.id ?? "unknown"} size="md" />
        <div className="flex-1">
          <MentionInput
            value={content}
            onChange={setContent}
            placeholder="Share an update with your team... Use @ to mention someone"
            rows={3}
            onSubmit={handleSubmit}
          />
          <div className="mt-3 flex items-center justify-between">
            <p className="text-xs text-muted-foreground">⌘ + Enter to post</p>
            <Button
              variant="primary"
              className="h-9 px-4"
              onClick={handleSubmit}
              disabled={!content.trim() || createPostMutation.isPending}
            >
              <Send size={15} />
              Post
            </Button>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
