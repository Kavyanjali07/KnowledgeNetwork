import { del, get, post, put } from "../lib/api-client";
import type { WorkspaceResponse } from "./graphApi";

export interface ProfileStatistics {
  graphsOwned: number;
  nodesCreated: number;
  edgesAuthored: number;
  posts: number;
  followers: number;
  following: number;
}

export interface ProfileResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  bio?: string;
  status?: string;
  role: string;
  emailVerified: boolean;
  createdAt: string;
  updatedAt: string;
  statistics: ProfileStatistics;
  graphs: WorkspaceResponse[];
}

export interface ProfileUpdateData {
  firstName: string;
  lastName: string;
  bio?: string;
  status?: string;
}

export const userApi = {
  me: () => get<ProfileResponse>("/profile/me"),
  getUser: (userId: string) => get<ProfileResponse>(`/profile/users/${userId}`),
  updateMe: (data: ProfileUpdateData) => put<ProfileResponse>("/profile/me", data),
  followUser: (followeeId: string) =>
    post<void>("/social/follow", undefined, { params: { followeeId } }),
  unfollowUser: (followeeId: string) =>
    del<void>("/social/follow", { params: { followeeId } }),
  isFollowing: (followeeId: string) =>
    get<boolean>("/social/follow/status", { params: { followeeId } })
};

export const profileApi = userApi;
