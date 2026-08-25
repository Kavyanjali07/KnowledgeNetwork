import type { NotificationItem } from "../types";
import type { NotificationItem as ApiNotificationItem } from "../../../services";

function randomId(prefix: string, index: number) {
  return `${prefix}-${index}`;
}

function randomBetween(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function offsetDate(days: number, hours = 0, minutes = 0) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  date.setHours(date.getHours() - hours);
  date.setMinutes(date.getMinutes() - minutes);
  return date.toISOString();
}

export function generateNotifications(count: number): NotificationItem[] {
  const templates: Array<Omit<NotificationItem, "id" | "read" | "timestamp">> = [
    { type: "like", actorId: "user-maya", entityType: "Graph", entityName: "Product Intelligence" },
    { type: "comment", actorId: "user-ari", entityType: "Document", entityName: "Ontology v18", commentPreview: "Great work on the migration guide — can we discuss temporal edges?" },
    { type: "fork", actorId: "user-leo", entityType: "Graph", entityName: "Engineering Systems", entityUrl: "/graphs/engineering-systems" },
    { type: "follow", actorId: "user-ava", entityType: "User", entityName: "Kavya Nair" },
    { type: "mention", actorId: "user-sam", entityType: "Graph", entityName: "Market Map", commentPreview: "@kavya can you sanity-check the merge candidates?" },
    { type: "like", actorId: "user-ava", entityType: "Post", entityName: "Completed ontology cleanup sprint" },
    { type: "comment", actorId: "user-maya", entityType: "Cluster", entityName: "Customer Signals", commentPreview: "This changes our Q3 priority map significantly." },
    { type: "fork", actorId: "user-ari", entityType: "Graph", entityName: "Decision Archive", entityUrl: "/graphs/decision-archive" },
    { type: "mention", actorId: "user-leo", entityType: "Post", entityName: "AI duplicate detection", commentPreview: "@kavya I auto-merged 3 duplicate concepts, please review." },
    { type: "follow", actorId: "user-sam", entityType: "User", entityName: "Kavya Nair" },
    { type: "like", actorId: "user-leo", entityType: "Document", entityName: "pgvector Indexing Architecture" },
    { type: "comment", actorId: "user-ava", entityType: "Graph", entityName: "Research Memory", commentPreview: "Embedding similarity picked up duplicates. Should I auto-merge?" }
  ];

  const items: NotificationItem[] = [];
  for (let i = 0; i < count; i++) {
    const template = templates[i % templates.length]!;
    items.push({
      id: randomId("notif", i),
      type: template.type,
      actorId: template.actorId,
      entityType: template.entityType,
      entityName: template.entityName,
      entityUrl: template.entityUrl,
      commentPreview: template.commentPreview,
      read: i >= 8,
      timestamp: offsetDate(randomBetween(0, 14), randomBetween(0, 23), randomBetween(0, 59))
    });
  }

  return items.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
}

export function mapNotificationItem(api: ApiNotificationItem): NotificationItem {
  return {
    id: api.id,
    type: api.type as NotificationItem["type"],
    actorId: api.recipientId,
    entityType: api.type,
    entityName: api.message,
    entityUrl: undefined,
    commentPreview: undefined,
    read: api.isRead,
    timestamp: api.createdAt
  };
}

export function mapNotificationItems(apiItems: ApiNotificationItem[]): NotificationItem[] {
  return apiItems.map(mapNotificationItem);
}

export function getUnreadCount(notifications: NotificationItem[]) {
  return notifications.filter((n) => !n.read).length;
}