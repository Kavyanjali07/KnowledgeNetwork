export type UserStatus = "online" | "away" | "offline";

export type SocialUser = {
  id: string;
  name: string;
  username: string;
  initials: string;
  gradient: string;
  bio: string;
  status: UserStatus;
  stats: {
    posts: number;
    followers: number;
    following: number;
  };
};

export const REACTION_EMOJIS = ["👍", "❤️", "🔥", "🎉", "💡", "🚀"] as const;
export type ReactionEmoji = (typeof REACTION_EMOJIS)[number];

export type Reaction = {
  emoji: ReactionEmoji;
  userIds: string[];
};

export type SocialComment = {
  id: string;
  authorId: string;
  authorName?: string;
  authorUsername?: string;
  content: string;
  createdAt: string;
  parentId: string | null;
  reactions: Reaction[];
  likes: number;
  likedByCurrentUser: boolean;
};

export type SocialPost = {
  id: string;
  workspaceId: string;
  authorId: string;
  authorName?: string;
  authorUsername?: string;
  content: string;
  entityType: string;
  entityName: string;
  createdAt: string;
  likes: number;
  likedByCurrentUser: boolean;
  reactions: Reaction[];
  comments: SocialComment[];
  favorites: number;
  favoritedByCurrentUser: boolean;
  commentCount: number;
};

export type FeedPage = {
  posts: SocialPost[];
  hasMore: boolean;
  nextCursor: number;
};
