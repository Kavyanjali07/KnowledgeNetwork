import { AnimatePresence, motion } from "framer-motion";
import { useCallback, useRef, useState } from "react";
import { cn } from "../../../lib/cn";
import { socialUsers } from "../data/users";
import { getMentionQuery, insertMention } from "../utils/mentions";
import { Avatar } from "./Avatar";

type MentionInputProps = {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  rows?: number;
  className?: string;
  onSubmit?: () => void;
};

export function MentionInput({
  value,
  onChange,
  placeholder = "Write a comment... Use @ to mention",
  rows = 2,
  className,
  onSubmit
}: MentionInputProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [mentionStart, setMentionStart] = useState<number | null>(null);
  const [mentionQuery, setMentionQuery] = useState("");
  const [selectedIndex, setSelectedIndex] = useState(0);

  const suggestions = mentionStart !== null
    ? socialUsers.filter(
        (user) =>
          user.username.toLowerCase().includes(mentionQuery.toLowerCase()) ||
          user.name.toLowerCase().includes(mentionQuery.toLowerCase())
      ).slice(0, 5)
    : [];

  const handleChange = useCallback(
    (event: React.ChangeEvent<HTMLTextAreaElement>) => {
      const newValue = event.target.value;
      const cursor = event.target.selectionStart ?? newValue.length;
      onChange(newValue);

      const mention = getMentionQuery(newValue, cursor);
      if (mention) {
        setMentionStart(mention.start);
        setMentionQuery(mention.query);
        setSelectedIndex(0);
      } else {
        setMentionStart(null);
        setMentionQuery("");
      }
    },
    [onChange]
  );

  const selectUser = useCallback(
    (username: string) => {
      if (mentionStart === null) return;
      const { text, cursor: newCursor } = insertMention(value, mentionStart, username);
      onChange(text);
      setMentionStart(null);
      setMentionQuery("");
      requestAnimationFrame(() => {
        textareaRef.current?.focus();
        textareaRef.current?.setSelectionRange(newCursor, newCursor);
      });
    },
    [mentionStart, onChange, value]
  );

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
      if (suggestions.length > 0 && mentionStart !== null) {
        if (event.key === "ArrowDown") {
          event.preventDefault();
          setSelectedIndex((index) => (index + 1) % suggestions.length);
          return;
        }
        if (event.key === "ArrowUp") {
          event.preventDefault();
          setSelectedIndex((index) => (index - 1 + suggestions.length) % suggestions.length);
          return;
        }
        if (event.key === "Enter" && !event.shiftKey) {
          event.preventDefault();
          const selectedUser = suggestions[selectedIndex];
          if (selectedUser) {
            selectUser(selectedUser.username);
          }
          return;
        }
        if (event.key === "Escape") {
          setMentionStart(null);
          return;
        }
      }

      if (event.key === "Enter" && (event.metaKey || event.ctrlKey) && onSubmit) {
        event.preventDefault();
        onSubmit();
      }
    },
    [mentionStart, onSubmit, selectUser, selectedIndex, suggestions]
  );

  return (
    <div className={cn("relative", className)}>
      <textarea
        ref={textareaRef}
        value={value}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        rows={rows}
        className="w-full resize-none rounded-lg border border-white/10 bg-black/20 px-3 py-2.5 text-sm leading-6 text-foreground outline-none transition placeholder:text-muted-foreground focus:border-cyan-300/35 focus:ring-2 focus:ring-cyan-300/15"
      />
      <AnimatePresence>
        {suggestions.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: -4 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            transition={{ duration: 0.15 }}
            className="absolute bottom-full left-0 z-50 mb-1 w-full overflow-hidden rounded-lg border border-white/12 bg-[rgba(9,13,20,0.96)] shadow-glass backdrop-blur-2xl"
          >
            {suggestions.map((user, index) => (
              <button
                key={user.id}
                type="button"
                className={cn(
                  "flex w-full items-center gap-2.5 px-3 py-2 text-left text-sm transition",
                  index === selectedIndex ? "bg-cyan-300/12 text-foreground" : "hover:bg-white/6"
                )}
                onMouseDown={(event) => {
                  event.preventDefault();
                  selectUser(user.username);
                }}
              >
                <Avatar user={user} size="xs" />
                <div className="min-w-0">
                  <p className="font-medium">{user.name}</p>
                  <p className="text-xs text-muted-foreground">@{user.username}</p>
                </div>
              </button>
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
