import type { SocialComment } from "../types";

export function buildCommentTree(comments: SocialComment[]): SocialComment[] {
  const map = new Map<string, SocialComment & { replies: SocialComment[] }>();
  const roots: (SocialComment & { replies: SocialComment[] })[] = [];

  for (const comment of comments) {
    map.set(comment.id, { ...comment, replies: [] });
  }

  for (const comment of comments) {
    const node = map.get(comment.id)!;
    if (comment.parentId && map.has(comment.parentId)) {
      map.get(comment.parentId)!.replies.push(node);
    } else {
      roots.push(node);
    }
  }

  return roots;
}

export function countAllComments(comments: SocialComment[]): number {
  return comments.length;
}
