import { del, get, post, put } from "../lib/api-client";

export interface EdgeTypeResponse {
  id: string;
  workspaceId: string;
  name: string;
  directed: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface EdgeResponse {
  id: string;
  workspaceId: string;
  edgeTypeId: string;
  sourceNodeId: string;
  targetNodeId: string;
  weight: number;
  attributes: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  version: number;
  deleted: boolean;
}

export const edgeTypeApi = {
  list: (workspaceId: string) =>
    get<any>(`/relationships/types/edges/${workspaceId}`).then((response) => ({
      data: response.data?.content ?? response.data
    }) as { data: EdgeTypeResponse[] }),
  create: (workspaceId: string, name: string, directed: boolean) =>
    post<EdgeTypeResponse>("/relationships/types/edges", { workspaceId, name, directed }),
  update: (workspaceId: string, edgeTypeId: string, name: string, directed: boolean, version: number) =>
    put<EdgeTypeResponse>(`/relationships/types/edges/${workspaceId}/${edgeTypeId}`, { name, directed, version })
};

export const edgeApi = {
  list: (workspaceId: string, page: number = 0, size: number = 100) =>
    get<{ content: EdgeResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>(`/relationships/edges/${workspaceId}`, { params: { page, size } }),
  create: (workspaceId: string, edgeTypeId: string, sourceNodeId: string, targetNodeId: string, weight?: number, attributes?: Record<string, unknown>) =>
    post<EdgeResponse>("/relationships/edges", { workspaceId, edgeTypeId, sourceNodeId, targetNodeId, weight, attributes }),
  update: (workspaceId: string, edgeId: string, weight?: number, attributes?: Record<string, unknown>, version?: number) =>
    put<EdgeResponse>(`/relationships/edges/${workspaceId}/${edgeId}`, { weight, attributes, version }),
  delete: (workspaceId: string, edgeId: string) =>
    del<void>(`/relationships/edges/${workspaceId}/${edgeId}`)
};
