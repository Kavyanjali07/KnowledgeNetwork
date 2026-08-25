import { del, get, post, put } from "../lib/api-client";

export interface NodeTypeResponse {
  id: string;
  workspaceId: string;
  name: string;
  colorCode: string;
  icon: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface NodeResponse {
  id: string;
  workspaceId: string;
  nodeTypeId: string;
  nodeTypeName?: string;
  nodeTypeColor?: string;
  nodeTypeIcon?: string;
  label: string;
  title?: string;
  positionX?: number;
  positionY?: number;
  attributes: Record<string, unknown>;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  version: number;
  deleted: boolean;
}

export const nodeTypeApi = {
  list: (workspaceId: string) =>
    get<any>(`/relationships/types/nodes/${workspaceId}`).then((response) => ({
      data: response.data?.content ?? response.data
    }) as { data: NodeTypeResponse[] }),
  create: (workspaceId: string, name: string, colorCode: string, icon: string) =>
    post<NodeTypeResponse>("/relationships/types/nodes", { workspaceId, name, colorCode, icon }),
  update: (workspaceId: string, nodeTypeId: string, name: string, colorCode: string, icon: string, version: number) =>
    put<NodeTypeResponse>(`/relationships/types/nodes/${workspaceId}/${nodeTypeId}`, { name, colorCode, icon, version })
};

export const nodeApi = {
  list: (graphId: string, page: number = 0, size: number = 200) =>
    get<{ content: NodeResponse[]; totalElements: number; totalPages: number; pageNumber: number; pageSize: number }>(`/graphs/${graphId}/nodes`, { params: { page, size } }),
  get: (nodeId: string) =>
    get<NodeResponse>(`/nodes/${nodeId}`),
  create: (graphId: string, nodeTypeId?: string, label?: string, attributes?: Record<string, unknown>, positionX?: number, positionY?: number) =>
    post<NodeResponse>(`/graphs/${graphId}/nodes`, { nodeTypeId, label: label || "New Node", attributes, positionX, positionY }),
  update: (_graphId: string, nodeId: string, label: string, attributes: Record<string, unknown>, version: number, positionX?: number, positionY?: number) =>
    put<NodeResponse>(`/nodes/${nodeId}`, { label, attributes, version, positionX, positionY }),
  delete: (_graphId: string, nodeId: string) =>
    del<void>(`/nodes/${nodeId}`)
};
