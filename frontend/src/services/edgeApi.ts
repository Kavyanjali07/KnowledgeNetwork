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
  graphId?: string;
  workspaceId: string;
  edgeTypeId?: string;
  edgeTypeName?: string;
  relationshipType?: string;
  directed?: boolean;
  sourceNodeId: string;
  sourceNodeLabel?: string;
  sourceNodeTitle?: string;
  targetNodeId: string;
  targetNodeLabel?: string;
  targetNodeTitle?: string;
  label?: string;
  description?: string;
  weight: number;
  attributes: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  version: number;
  deleted: boolean;
}

export interface EdgeCreatePayload {
  sourceNodeId: string;
  targetNodeId: string;
  relationshipType?: string;
  edgeTypeId?: string;
  label?: string;
  description?: string;
  weight?: number;
  attributes?: Record<string, unknown>;
}

export interface EdgeUpdatePayload {
  relationshipType?: string;
  edgeTypeId?: string;
  label?: string;
  description?: string;
  weight?: number;
  attributes?: Record<string, unknown>;
  version: number;
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
  list: (graphId: string) =>
    get<EdgeResponse[] | { content: EdgeResponse[] }>(`/graphs/${graphId}/edges`).then((response) => ({
      data: Array.isArray(response.data) ? response.data : response.data?.content ?? []
    })),
  getById: (edgeId: string) =>
    get<EdgeResponse>(`/edges/${edgeId}`),
  create: (graphId: string, payload: EdgeCreatePayload) =>
    post<EdgeResponse>(`/graphs/${graphId}/edges`, payload),
  update: (edgeId: string, payload: EdgeUpdatePayload) =>
    put<EdgeResponse>(`/edges/${edgeId}`, payload),
  delete: (edgeId: string) =>
    del<void>(`/edges/${edgeId}`)
};
