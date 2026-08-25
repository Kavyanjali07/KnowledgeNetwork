import { cn } from "../../../lib/cn";
import { getUserByUsername } from "../data/users";
import { parseMentions } from "../utils/mentions";

type MentionTextProps = {
  content: string;
  className?: string;
};

export function MentionText({ content, className }: MentionTextProps) {
  const segments = parseMentions(content);

  return (
    <span className={cn("whitespace-pre-wrap break-words", className)}>
      {segments.map((segment, index) => {
        if (segment.type === "text") {
          return <span key={index}>{segment.value}</span>;
        }

        const user = getUserByUsername(segment.username);
        return (
          <button
            key={index}
            type="button"
            className="rounded px-0.5 font-medium text-cyan-300 transition hover:bg-cyan-300/10 hover:text-cyan-200"
            title={user ? user.name : `@${segment.username}`}
          >
            {segment.value}
          </button>
        );
      })}
    </span>
  );
}
