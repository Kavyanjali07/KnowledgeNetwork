export type NotificationType = "like" | "comment" | "fork" | "follow" | "mention" | "system";

export type NotificationGroupKey = "today" | "yesterday" | "thisWeek" | "older";

export type NotificationItem = {
  id: string;
  type: NotificationType;
  actorId: string;
  entityType: string;
  entityName: string;
  entityUrl?: string;
  commentPreview?: string;
  read: boolean;
  timestamp: string;
};

export type NotificationsState = {
  notifications: NotificationItem[];
  unreadCount: number;
  lastReadTimestamp: number;
};

export type NotificationsPage = {
  items: NotificationItem[];
  hasMore: boolean;
  nextCursor: number;
};
