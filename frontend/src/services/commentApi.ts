import { del, get, post } from "../lib/api-client";

export interface SocialPostResponse {
  id: string;
  workspaceId: string;
  authorId: string;
  author: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  content: string;
  entityType: string;
  entityId: string;
  likeCount: number;
  isLikedByCurrentUser: boolean;
  favoriteCount: number;
  isFavoritedByCurrentUser: boolean;
  commentCount: number;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface SocialCommentResponse {
  id: string;
  workspaceId: string;
  postId: string;
  authorId: string;
  author: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  content: string;
  parentCommentId: string | null;
  createdAt: string;
  updatedAt: string;
  replies: SocialCommentResponse[];
}

export const commentApi = {
  createPost: (workspaceId: string, content: string, entityType: string, entityId: string) =>
    post<SocialPostResponse>("/social/posts", undefined, { params: { workspaceId, content, entityType, entityId } }),
  getPosts: (workspaceId: string, page: number = 0, size: number = 20) =>
    get<{ content: SocialPostResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>(`/social/posts/${workspaceId}`, { params: { page, size } }),
  likePost: (workspaceId: string, postId: string) =>
    post<SocialPostResponse>(`/social/posts/${postId}/like`, undefined, { params: { workspaceId } }),
  unlikePost: (workspaceId: string, postId: string) =>
    del<void>(`/social/posts/${postId}/unlike`, { params: { workspaceId } }),
  favoritePost: (workspaceId: string, postId: string) =>
    post<SocialPostResponse>(`/social/posts/${postId}/favorite`, undefined, { params: { workspaceId } }),
  unfavoritePost: (workspaceId: string, postId: string) =>
    del<void>(`/social/posts/${postId}/unfavorite`, { params: { workspaceId } }),
  addComment: (postId: string, content: string, parentCommentId?: string) =>
    post<SocialCommentResponse>(`/social/posts/${postId}/comments`, { content, parentCommentId }),
  getComments: (workspaceId: string, postId: string) =>
    get<{ content: SocialCommentResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>(`/social/posts/${postId}/comments`, { params: { workspaceId } }),
  addTag: (workspaceId: string, postId: string, tag: string) =>
    post<void>(`/social/posts/${postId}/tags`, undefined, { params: { tag, workspaceId } }),
  getSummary: (workspaceId: string, postId: string) =>
    get<{ postId: string; likes: number; favorites: number; comments: number; tags: string[] }>(`/social/posts/${postId}/summary`, { params: { workspaceId } })
};

export const socialApi = commentApi;
