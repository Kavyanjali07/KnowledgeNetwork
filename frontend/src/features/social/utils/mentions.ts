const MENTION_PATTERN = /@(\w+)/g;

export function extractMentions(text: string): string[] {
  const matches = text.matchAll(MENTION_PATTERN);
  return [...matches].flatMap((match) => (match[1] ? [match[1]] : []));
}

export type MentionSegment =
  | { type: "text"; value: string }
  | { type: "mention"; username: string; value: string };

export function parseMentions(text: string): MentionSegment[] {
  const segments: MentionSegment[] = [];
  let lastIndex = 0;

  for (const match of text.matchAll(MENTION_PATTERN)) {
    const index = match.index ?? 0;
    if (index > lastIndex) {
      segments.push({ type: "text", value: text.slice(lastIndex, index) });
    }
    const username = match[1];
    if (username) {
      segments.push({ type: "mention", username, value: match[0] });
    }
    lastIndex = index + match[0].length;
  }

  if (lastIndex < text.length) {
    segments.push({ type: "text", value: text.slice(lastIndex) });
  }

  return segments.length > 0 ? segments : [{ type: "text", value: text }];
}

export function getMentionQuery(text: string, cursorPosition: number): { query: string; start: number } | null {
  const beforeCursor = text.slice(0, cursorPosition);
  const match = beforeCursor.match(/@(\w*)$/);
  if (!match) return null;
  return {
    query: match[1] ?? "",
    start: beforeCursor.length - match[0].length
  };
}

export function insertMention(text: string, start: number, username: string): { text: string; cursor: number } {
  const before = text.slice(0, start);
  const afterMatch = text.slice(start).match(/^@\w*/);
  const after = afterMatch ? text.slice(start + afterMatch[0].length) : text.slice(start);
  const mention = `@${username} `;
  return {
    text: before + mention + after,
    cursor: before.length + mention.length
  };
}
