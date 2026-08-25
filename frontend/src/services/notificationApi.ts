import { get, patch } from "../lib/api-client";

export interface NotificationItem {
  id: string;
  workspaceId: string;
  type: string;
  recipientId: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationListResponse {
  content: NotificationItem[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

export const notificationApi = {
  list: (page: number = 0, size: number = 20) =>
    get<NotificationListResponse>("/notifications", { params: { page, size } }),
  getUnreadCount: () =>
    get<number>("/notifications/unread-count"),
  markAsRead: (id: string) =>
    patch<void>(`/notifications/${id}/read`, {}),
  markAllAsRead: () =>
    patch<void>("/notifications/read-all", {})
};
