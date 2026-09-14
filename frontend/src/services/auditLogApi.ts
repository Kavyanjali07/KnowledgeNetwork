import apiClient from "../lib/api-client";

export interface AuditLogResponse {
  id: string;
  workspaceId?: string;
  actorId?: string;
  actorEmail: string;
  actorName: string;
  action: string;
  entityType: string;
  entityId?: string;
  metadata: Record<string, any>;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AuditLogQueryParams {
  workspaceId?: string;
  actorId?: string;
  action?: string;
  entityType?: string;
  entityId?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export const auditLogApi = {
  getLogs: async (params?: AuditLogQueryParams): Promise<PageResponse<AuditLogResponse>> => {
    const response = await apiClient.get<PageResponse<AuditLogResponse>>("/audit-logs", { params });
    return response.data;
  }
};
