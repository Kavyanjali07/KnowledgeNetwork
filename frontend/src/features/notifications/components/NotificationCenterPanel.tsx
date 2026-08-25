import { AnimatePresence, motion } from "framer-motion";
import { Bell, Check, Loader2, Settings2 } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notificationApi } from "../../../services";
import { useToast } from "../../../components/ui/toast";
import { NotificationGroup } from "./NotificationGroup";
import type { NotificationGroupKey, NotificationItem } from "../types";
import { mapNotificationItems } from "../data/notifications";

const PAGE_SIZE = 12;

function groupNotifications(
  items: NotificationItem[]
): Record<NotificationGroupKey, NotificationItem[]> {
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterdayStart = new Date(todayStart);
  yesterdayStart.setDate(yesterdayStart.getDate() - 1);
  const weekStart = new Date(todayStart);
  weekStart.setDate(weekStart.getDate() - 7);

  const groups: Record<NotificationGroupKey, NotificationItem[]> = {
    today: [],
    yesterday: [],
    thisWeek: [],
    older: []
  };

  items.forEach((item) => {
    const date = new Date(item.timestamp);
    if (date >= todayStart) {
      groups.today.push(item);
    } else if (date >= yesterdayStart) {
      groups.yesterday.push(item);
    } else if (date >= weekStart) {
      groups.thisWeek.push(item);
    } else {
      groups.older.push(item);
    }
  });

  return groups;
}

export function NotificationCenterPanel({
  open,
  onClose,
  onOpenHistory
}: {
  open: boolean;
  onClose: () => void;
  onOpenHistory: () => void;
}) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  const [initializing, setInitializing] = useState(true);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const { data, isError, error } = useQuery({
    queryKey: ["notifications"],
    queryFn: () => notificationApi.list(0, PAGE_SIZE).then((r) => r.data),
    enabled: open,
    staleTime: 30000
  });

  useEffect(() => {
    if (!open) return;
    if (data) {
      const mapped = mapNotificationItems(data.content);
      setNotifications(mapped);
      setUnreadCount(mapped.filter((n) => !n.read).length);
      setInitializing(false);
    }
  }, [data, open]);

  const markAsReadMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notifications-unread-count"] });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to mark as read", description: error.message });
    }
  });

  useEffect(() => {
    if (!open) return;
    setInitializing(true);
    setNotifications([]);
    queryClient.invalidateQueries({ queryKey: ["notifications"] });
  }, [open, queryClient]);

  function markRead(id: string) {
    markAsReadMutation.mutate(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
  }

  function markAllRead() {
    notificationApi.markAllAsRead().then(() => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notifications-unread-count"] });
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    }).catch((error: any) => {
      addToast({ type: "error", title: "Failed to mark all as read", description: error.message });
    });
  }

  function handleNavigate(id: string) {
    markRead(id);
    onClose();
  }

  const grouped = groupNotifications(notifications);
  const groupOrder: NotificationGroupKey[] = ["today", "yesterday", "thisWeek", "older"];
  const totalUnread = unreadCount;

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="absolute right-0 top-12 z-50 w-[360px] rounded-2xl border border-white/12 bg-[rgba(8,12,20,0.96)] shadow-glass backdrop-blur-2xl"
          initial={{ opacity: 0, y: -12, scale: 0.98 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -12, scale: 0.98 }}
          transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          <div className="flex items-center justify-between border-b border-white/10 px-4 py-3">
            <div className="flex items-center gap-2">
              <Bell size={16} className="text-cyan-300" />
              <span className="text-sm font-semibold">Notifications</span>
              {totalUnread > 0 && (
                <span className="rounded-full border border-cyan-500/40 bg-cyan-500/15 px-2 py-0.5 text-[11px] text-cyan-200">
                  {totalUnread}
                </span>
              )}
            </div>
            <button
              onClick={markAllRead}
              className="flex items-center gap-1 rounded-lg border border-white/10 px-2 py-1 text-[11px] text-slate-300 transition hover:border-emerald-300/40 hover:text-emerald-200"
              title="Mark all as read"
            >
              <Check size={11} />
              Mark all read
            </button>
          </div>

          <div className="max-h-[70vh] overflow-y-auto p-3 scrollbar-none">
            {initializing && (
              <div className="flex justify-center py-10">
                <Loader2 size={18} className="animate-spin text-cyan-300" />
              </div>
            )}
            {!initializing && isError && (
              <p className="py-10 text-center text-xs text-muted-foreground">
                {(error as any)?.message || "Failed to load notifications."}
              </p>
            )}
            {!initializing && !isError && notifications.length === 0 && (
              <p className="py-10 text-center text-xs text-muted-foreground">You&apos;re all caught up.</p>
            )}
            {!initializing && !isError &&
              notifications.length > 0 &&
              groupOrder.map((groupKey) => {
                const items = grouped[groupKey];
                if (items.length === 0) return null;
                return (
                  <NotificationGroup
                    key={groupKey}
                    groupKey={groupKey}
                    items={items}
                    onMarkRead={markRead}
                    onNavigate={handleNavigate}
                  />
                );
              })}
            <div ref={sentinelRef} className="h-1" />
          </div>

          <div className="border-t border-white/10 px-4 py-2.5">
            <button
              onClick={() => {
                onClose();
                onOpenHistory();
              }}
              className="flex w-full items-center gap-2 rounded-lg border border-white/10 px-3 py-2 text-xs text-slate-300 transition hover:border-cyan-300/40 hover:text-cyan-200"
            >
              <Settings2 size={13} />
              View all notifications
            </button>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}