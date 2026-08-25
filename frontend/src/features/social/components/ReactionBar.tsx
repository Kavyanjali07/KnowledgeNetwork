import { AnimatePresence, motion } from "framer-motion";
import { Plus, Smile } from "lucide-react";
import { useCallback, useRef, useState } from "react";
import { cn } from "../../../lib/cn";
import { useAuth } from "../../../lib/auth-context";
import type { Reaction, ReactionEmoji } from "../types";
import { REACTION_EMOJIS } from "../types";

type ReactionBarProps = {
  reactions: Reaction[];
  onToggleReaction: (emoji: ReactionEmoji) => void;
  compact?: boolean;
};

export function ReactionBar({ reactions, onToggleReaction, compact = false }: ReactionBarProps) {
  const { currentUser } = useAuth();
  const [pickerOpen, setPickerOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const hasUserReacted = useCallback(
    (reaction: Reaction) => reaction.userIds.includes(currentUser?.id ?? ""),
    [currentUser?.id]
  );

  const handleTogglePicker = useCallback(() => {
    setPickerOpen((value) => !value);
  }, []);

  const handleSelect = useCallback(
    (emoji: ReactionEmoji) => {
      onToggleReaction(emoji);
      setPickerOpen(false);
    },
    [onToggleReaction]
  );

  return (
    <div ref={containerRef} className="relative flex flex-wrap items-center gap-1.5">
      <AnimatePresence mode="popLayout">
        {reactions
          .filter((reaction) => reaction.userIds.length > 0)
          .map((reaction) => {
            const active = hasUserReacted(reaction);
            return (
              <motion.button
                key={reaction.emoji}
                layout
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.8 }}
                whileHover={{ scale: 1.06 }}
                whileTap={{ scale: 0.94 }}
                type="button"
                onClick={() => onToggleReaction(reaction.emoji)}
                className={cn(
                  "inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs transition",
                  active
                    ? "border-cyan-300/30 bg-cyan-300/12 text-cyan-100"
                    : "border-white/10 bg-black/16 text-muted-foreground hover:border-white/18 hover:bg-white/6"
                )}
              >
                <span>{reaction.emoji}</span>
                <span className="tabular-nums">{reaction.userIds.length}</span>
              </motion.button>
            );
          })}
      </AnimatePresence>

      <div className="relative">
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          type="button"
          onClick={handleTogglePicker}
          className={cn(
            "inline-flex items-center justify-center rounded-full border border-white/10 bg-black/16 text-muted-foreground transition hover:border-cyan-300/25 hover:bg-white/6 hover:text-foreground",
            compact ? "h-6 w-6" : "h-7 w-7"
          )}
          aria-label="Add reaction"
        >
          {pickerOpen ? <Plus size={compact ? 12 : 14} className="rotate-45" /> : <Smile size={compact ? 12 : 14} />}
        </motion.button>

        <AnimatePresence>
          {pickerOpen && (
            <motion.div
              initial={{ opacity: 0, y: 4, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 4, scale: 0.95 }}
              transition={{ duration: 0.15, ease: [0.16, 1, 0.3, 1] }}
              className="absolute bottom-full left-0 z-50 mb-2 flex gap-0.5 rounded-full border border-white/12 bg-[rgba(9,13,20,0.96)] p-1 shadow-glass backdrop-blur-2xl"
            >
              {REACTION_EMOJIS.map((emoji) => (
                <motion.button
                  key={emoji}
                  type="button"
                  whileHover={{ scale: 1.2, y: -2 }}
                  whileTap={{ scale: 0.9 }}
                  onClick={() => handleSelect(emoji)}
                  className="flex h-8 w-8 items-center justify-center rounded-full text-lg transition hover:bg-white/8"
                >
                  {emoji}
                </motion.button>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

export function toggleReaction(reactions: Reaction[], emoji: ReactionEmoji, userId: string): Reaction[] {
  const existing = reactions.find((reaction) => reaction.emoji === emoji);

  if (existing) {
    const hasReacted = existing.userIds.includes(userId);
    const updatedUserIds = hasReacted
      ? existing.userIds.filter((id) => id !== userId)
      : [...existing.userIds, userId];

    if (updatedUserIds.length === 0) {
      return reactions.filter((reaction) => reaction.emoji !== emoji);
    }

    return reactions.map((reaction) =>
      reaction.emoji === emoji ? { ...reaction, userIds: updatedUserIds } : reaction
    );
  }

  return [...reactions, { emoji, userIds: [userId] }];
}
