import { useQuery } from "@tanstack/react-query";
import { edgeApi, graphApi, nodeApi, workspaceApi } from "../services";
import type { EdgeResponse, NodeResponse } from "../services";

export function useWorkspaces() {
  return useQuery({
    queryKey: ["workspaces"],
    queryFn: () => workspaceApi.list().then((response) => response.data),
    retry: false
  });
}

export function useGraphs(params?: { page?: number; size?: number; sort?: string; direction?: string; visibility?: string }) {
  return useQuery({
    queryKey: ["graphs", params],
    queryFn: () => graphApi.list(params).then((response) => response.data),
    retry: false
  });
}

export function useGraph(id: string) {
  return useQuery({
    queryKey: ["graph", id],
    queryFn: () => graphApi.get(id).then((response) => response.data),
    enabled: Boolean(id),
    retry: false
  });
}

export function useGraphProvenance(workspaceId: string) {
  return useQuery({
    queryKey: ["graph-provenance", workspaceId],
    queryFn: () => graphApi.getProvenance(workspaceId).then((response) => response.data),
    enabled: Boolean(workspaceId),
    retry: false
  });
}

export function useCurrentWorkspace() {
  const query = useWorkspaces();
  return {
    ...query,
    workspace: query.data?.[0] ?? null,
    workspaceId: query.data?.[0]?.id ?? null
  };
}

export function useWorkspaceNodes(workspaceId: string) {
  return useQuery({
    queryKey: ["workspace-nodes", workspaceId],
    queryFn: () => nodeApi.list(workspaceId, 0, 100).then((response) => ({
      nodes: response.data.content,
      total: response.data.totalElements
    })),
    enabled: Boolean(workspaceId)
  });
}

export function useWorkspaceEdges(workspaceId: string) {
  return useQuery({
    queryKey: ["workspace-edges", workspaceId],
    queryFn: () => edgeApi.list(workspaceId).then((response) => response.data),
    enabled: Boolean(workspaceId)
  });
}

export type WorkspaceNodesQuery = { nodes: NodeResponse[]; total: number };
export type WorkspaceEdgesQuery = { content: EdgeResponse[]; totalElements: number };
