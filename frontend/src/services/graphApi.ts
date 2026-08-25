import { del, get, post, put } from "../lib/api-client";
import type { NodeResponse } from "./nodeApi";

export interface WorkspaceResponse {
  id: string;
  name: string;
  title?: string;
  description: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  ownerId: string;
  ownerEmail: string;
  ownerName: string;
  createdAt: string;
  updatedAt: string;
  version: number;
  deleted: boolean;
}

export interface GraphResponse {
  id: string;
  title: string;
  name: string;
  description: string;
  ownerId: string;
  ownerEmail: string;
  ownerName: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  createdAt: string;
  updatedAt: string;
  version: number;
  deleted: boolean;
  nodeCount?: number;
  edgeCount?: number;
}

export interface WorkspaceMemberResponse {
  id: string;
  workspaceId: string;
  userId: string;
  userEmail: string;
  userName: string;
  role: string;
  joinedAt: string;
}

export interface SearchResponse {
  content: NodeResponse[];
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  last: boolean;
}

export interface VersionEntryResponse {
  id: string;
  workspaceId: string;
  snapshot: {
    id: string;
    name: string;
    description: string;
    nodeCount: number;
    edgeCount: number;
    createdAt: string;
    createdBy: string;
  };
  versionNumber: number;
  label: string;
  description: string;
  changeType: string;
  createdAt: string;
  createdBy: string;
}

export interface ForkResponse {
  id: string;
  workspaceId: string;
  sourceVersionId: string;
  name: string;
  description: string;
  createdAt: string;
  createdBy: string;
}

export const workspaceApi = {
  list: () => get<WorkspaceResponse[]>("/workspaces"),
  create: (name: string, description: string, visibility: 'PUBLIC' | 'PRIVATE' = 'PRIVATE') =>
    post<WorkspaceResponse>("/workspaces", { name, description, visibility }),
  get: (id: string) => get<WorkspaceResponse>(`/workspaces/${id}`),
  update: (id: string, name: string, description: string, version?: number, visibility?: 'PUBLIC' | 'PRIVATE') =>
    put<WorkspaceResponse>(`/workspaces/${id}`, { name, description, version, visibility }),
  delete: (id: string) => del<void>(`/workspaces/${id}`),
  listMembers: (id: string, page: number = 0, size: number = 20) =>
    get<WorkspaceMemberResponse[]>(`/workspaces/${id}/members`, { params: { page, size } }),
  addMember: (id: string, userId: string, role: string) =>
    post<WorkspaceMemberResponse>(`/workspaces/${id}/members`, { userId, role }),
  removeMember: (id: string, memberId: string) =>
    del<void>(`/workspaces/${id}/members/${memberId}`)
};

export const graphApi = {
  list: (params?: { page?: number; size?: number; sort?: string; direction?: string; visibility?: string }) =>
    get<{ content: GraphResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>("/graphs", { params }),
  get: (id: string) => get<GraphResponse>(`/graphs/${id}`),
  create: (data: { title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE' }) =>
    post<GraphResponse>("/graphs", data),
  update: (id: string, data: { title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE'; version?: number }) =>
    put<GraphResponse>(`/graphs/${id}`, data),
  delete: (id: string) => del<void>(`/graphs/${id}`),

  traverse: (workspaceId: string, rootNodeId: string, maxDepth?: number, edgeTypeNames?: string[], direction?: string) =>
    post<{ rootNodeId: string; visitedNodeCount: number; visitedEdgeCount: number; nodes: Array<{ id: string; label: string; type: string }>; edges: Array<{ id: string; source: string; target: string; type: string; weight: number }> }>("/relationships/traverse", { workspaceId, rootNodeId, maxDepth, edgeTypeNames, direction }),
  detectCycles: (workspaceId: string) =>
    get<{ workspaceId: string; cycleCount: number; cycles: Array<{ path: string[] }> }>(`/relationships/cycles/${workspaceId}`)
};

export const searchApi = {
  search: (
    workspaceId: string,
    query: string,
    tags?: string[],
    visibility?: string,
    page: number = 0,
    size: number = 20,
    filters?: { updatedAfter?: string; minConfidence?: number; sortBy?: string }
  ) => get<SearchResponse>("/graphs/search", {
    params: { workspaceId, q: query, tags, visibility, page, size, ...filters }
  })
};

export const versioningApi = {
  createSnapshot: (workspaceId: string, label: string, description: string) =>
    post<VersionEntryResponse>(`/graphs/${workspaceId}/snapshots`, { label, description }),
  listHistory: (workspaceId: string, page: number = 0, size: number = 20) =>
    get<{ content: VersionEntryResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>(`/graphs/${workspaceId}/versions`, { params: { page, size } }),
  getVersion: (workspaceId: string, versionId: string) =>
    get<VersionEntryResponse>(`/graphs/${workspaceId}/versions/${versionId}`),
  restoreVersion: (workspaceId: string, versionId: string) =>
    post<VersionEntryResponse>(`/graphs/${workspaceId}/versions/${versionId}/restore`, {}),
  createFork: (workspaceId: string, sourceVersionId: string, name: string, description: string) =>
    post<ForkResponse>(`/graphs/${workspaceId}/forks`, { sourceVersionId, name, description })
};
