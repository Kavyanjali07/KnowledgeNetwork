import { del, get, post, put } from "../lib/api-client";
import type { NodeResponse } from "./nodeApi";

export type LicenseType =
  | "ALL_RIGHTS_RESERVED"
  | "CC0_1_0"
  | "CC_BY_4_0"
  | "CC_BY_SA_4_0"
  | "CC_BY_NC_4_0"
  | "CC_BY_NC_SA_4_0"
  | "CC_BY_ND_4_0"
  | "CC_BY_NC_ND_4_0";

export interface LicenseInfo {
  type: LicenseType;
  shortName: string;
  fullName: string;
  description: string;
  allowsDerivatives: boolean;
  requiresShareAlike: boolean;
  allowsCommercial: boolean;
  requiresAttribution: boolean;
}

export const LICENSE_METADATA: Record<LicenseType, LicenseInfo> = {
  ALL_RIGHTS_RESERVED: {
    type: "ALL_RIGHTS_RESERVED",
    shortName: "All Rights Reserved",
    fullName: "All Rights Reserved",
    description: "No reuse, derivative work, or commercial distribution permitted without explicit owner consent.",
    allowsDerivatives: false,
    requiresShareAlike: false,
    allowsCommercial: true,
    requiresAttribution: true
  },
  CC0_1_0: {
    type: "CC0_1_0",
    shortName: "CC0 1.0",
    fullName: "CC0 1.0 Universal (Public Domain)",
    description: "Waive all copyright and related rights. Anyone can freely reuse, adapt, and build upon this network.",
    allowsDerivatives: true,
    requiresShareAlike: false,
    allowsCommercial: true,
    requiresAttribution: false
  },
  CC_BY_4_0: {
    type: "CC_BY_4_0",
    shortName: "CC BY 4.0",
    fullName: "Creative Commons Attribution 4.0 International",
    description: "Permits reuse, distribution, and derivatives for any purpose, provided appropriate attribution is given.",
    allowsDerivatives: true,
    requiresShareAlike: false,
    allowsCommercial: true,
    requiresAttribution: true
  },
  CC_BY_SA_4_0: {
    type: "CC_BY_SA_4_0",
    shortName: "CC BY-SA 4.0",
    fullName: "Creative Commons Attribution-ShareAlike 4.0 International",
    description: "Permits reuse and derivatives, provided attribution is given and derived works are licensed under identical terms.",
    allowsDerivatives: true,
    requiresShareAlike: true,
    allowsCommercial: true,
    requiresAttribution: true
  },
  CC_BY_NC_4_0: {
    type: "CC_BY_NC_4_0",
    shortName: "CC BY-NC 4.0",
    fullName: "Creative Commons Attribution-NonCommercial 4.0 International",
    description: "Permits reuse and derivatives for non-commercial purposes only, with attribution.",
    allowsDerivatives: true,
    requiresShareAlike: false,
    allowsCommercial: false,
    requiresAttribution: true
  },
  CC_BY_NC_SA_4_0: {
    type: "CC_BY_NC_SA_4_0",
    shortName: "CC BY-NC-SA 4.0",
    fullName: "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International",
    description: "Permits non-commercial reuse and derivatives with attribution, locked to the same ShareAlike license.",
    allowsDerivatives: true,
    requiresShareAlike: true,
    allowsCommercial: false,
    requiresAttribution: true
  },
  CC_BY_ND_4_0: {
    type: "CC_BY_ND_4_0",
    shortName: "CC BY-ND 4.0",
    fullName: "Creative Commons Attribution-NoDerivatives 4.0 International",
    description: "Permits redistribution for any purpose, provided the work is passed along unchanged and in whole with attribution.",
    allowsDerivatives: false,
    requiresShareAlike: false,
    allowsCommercial: true,
    requiresAttribution: true
  },
  CC_BY_NC_ND_4_0: {
    type: "CC_BY_NC_ND_4_0",
    shortName: "CC BY-NC-ND 4.0",
    fullName: "Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International",
    description: "Most restrictive CC license: permits downloading and sharing for non-commercial purposes with attribution, but no derivatives.",
    allowsDerivatives: false,
    requiresShareAlike: false,
    allowsCommercial: false,
    requiresAttribution: true
  }
};

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
  licenseType?: LicenseType;
  isPublished?: boolean;
  publishedAt?: string;
  customAttribution?: string;
  derivativeCount?: number;
  referenceCount?: number;
}

export interface GraphForkResponse {
  id: string;
  workspaceId: string;
  sourceWorkspaceId: string;
  sourceVersionId?: string;
  name: string;
  description?: string;
  createdAt: string;
  createdBy: string;
  sourceLicense?: LicenseType;
  originalCreatorId?: string;
  originalCreatorName?: string;
  isDerivative: boolean;
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

export interface SearchItemResponse {
  resultType: 'GRAPH' | 'NODE';
  id: string;
  title: string;
  description?: string;
  graphId: string;
  graphTitle: string;
  nodeTypeName?: string;
  nodeTypeColor?: string;
  nodeTypeIcon?: string;
  positionX?: number;
  positionY?: number;
  visibility?: 'PUBLIC' | 'PRIVATE';
  nodeCount?: number;
  edgeCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface UnifiedSearchResponse {
  content: SearchItemResponse[];
  totalElements: number;
  pageSize: number;
  pageNumber: number;
  totalPages: number;
  isLast: boolean;
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
  create: (data: { title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE'; licenseType?: LicenseType; customAttribution?: string }) =>
    post<GraphResponse>("/graphs", data),
  update: (id: string, data: { title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE'; version?: number; licenseType?: LicenseType; customAttribution?: string }) =>
    put<GraphResponse>(`/graphs/${id}`, data),
  delete: (id: string) => del<void>(`/graphs/${id}`),

  publish: (id: string, data?: { licenseType?: LicenseType; customAttribution?: string }) =>
    put<GraphResponse>(`/graphs/${id}/publish`, data),
  unpublish: (id: string) =>
    put<GraphResponse>(`/graphs/${id}/unpublish`, {}),
  getProvenance: (workspaceId: string) =>
    get<GraphForkResponse>(`/graphs/${workspaceId}/provenance`),

  traverse: (workspaceId: string, rootNodeId: string, maxDepth?: number, edgeTypeNames?: string[], direction?: string) =>
    post<{ rootNodeId: string; visitedNodeCount: number; visitedEdgeCount: number; nodes: Array<{ id: string; label: string; type: string }>; edges: Array<{ id: string; source: string; target: string; type: string; weight: number }> }>("/relationships/traverse", { workspaceId, rootNodeId, maxDepth, edgeTypeNames, direction }),
  detectCycles: (workspaceId: string) =>
    get<{ workspaceId: string; cycleCount: number; cycles: Array<{ path: string[] }> }>(`/relationships/cycles/${workspaceId}`)
};

export const searchApi = {
  unifiedSearch: (params: {
    q?: string;
    type?: 'all' | 'graphs' | 'nodes';
    nodeType?: string;
    page?: number;
    size?: number;
  }) => get<UnifiedSearchResponse>("/search", { params }),

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
