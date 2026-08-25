import { Bell, Check, Loader2 } from "lucide-react";
import { motion } from "framer-motion";
import { useMemo, useState } from "react";
import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { notificationApi } from "../../../services";
import { useToast } from "../../../components/ui/toast";
import { NotificationGroup } from "../components/NotificationGroup";
import type { NotificationGroupKey, NotificationItem } from "../types";
import { mapNotificationItems } from "../data/notifications";
import { EmptyState } from "../../../components/ui/empty-state";

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

export function NotificationHistoryPage() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const {
    data,
    isLoading,
    isError,
    error,
    isFetchingNextPage
  } = useInfiniteQuery({
    queryKey: ["notifications"],
    queryFn: ({ pageParam = 0 }) =>
      notificationApi.list(pageParam, PAGE_SIZE).then((r) => r.data),
    getNextPageParam: (lastPage) => {
      if (lastPage.pageNumber < lastPage.totalPages - 1) {
        return lastPage.pageNumber + 1;
      }
      return undefined;
    },
    initialPageParam: 0
  });

  const notifications: NotificationItem[] =
    data?.pages.flatMap((page) => mapNotificationItems(page.content)) ?? [];

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

  const unreadCount = notifications.filter((n) => !n.read).length;

  function markRead(id: string) {
    markAsReadMutation.mutate(id);
  }

  function markAllRead() {
    notificationApi.markAllAsRead().then(() => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notifications-unread-count"] });
    }).catch((error: any) => {
      addToast({ type: "error", title: "Failed to mark all as read", description: error.message });
    });
  }

  const filtered = useMemo(() => {
    let items = notifications;
    if (activeFilter) {
      items = items.filter((n) => n.type === activeFilter);
    }
    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      items = items.filter(
        (n) =>
          n.entityName.toLowerCase().includes(q) ||
          n.commentPreview?.toLowerCase().includes(q) ||
          n.type.toLowerCase().includes(q)
      );
    }
    return items;
  }, [notifications, activeFilter, searchQuery]);

  const grouped = groupNotifications(filtered);
  const groupOrder: NotificationGroupKey[] = ["today", "yesterday", "thisWeek", "older"];

  if (isLoading) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <motion.div
          className="mb-6 flex flex-wrap items-end justify-between gap-4"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
        >
          <div className="space-y-2">
            <div className="h-4 w-40 rounded-md bg-white/8 skeleton-shimmer" />
            <div className="h-7 w-64 rounded-md bg-white/8 skeleton-shimmer" />
            <div className="h-4 w-96 rounded-md bg-white/8 skeleton-shimmer" />
          </div>
        </motion.div>
        <div className="flex items-center justify-center py-12 sm:py-20">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-2 text-sm text-cyan-300"
          >
            <Loader2 size={18} className="animate-spin" />
            Loading notifications...
          </motion.div>
        </div>
      </section>
    );
  }

  if (isError) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <EmptyState
          icon={<Bell size={28} className="text-rose-300" />}
          title="Unable to load notifications"
          description={(error as any)?.message || "Failed to fetch notifications."}
        />
      </section>
    );
  }

  return (
    <section className="mx-auto mt-6 max-w-7xl pt-4">
      <motion.div
        className="mb-6 flex flex-wrap items-end justify-between gap-4"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-muted-foreground">Notification inbox</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Notifications</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
            Stay on top of likes, comments, forks, mentions, and follows across your knowledge graphs.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={markAllRead}
            className="flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-xs text-slate-300 transition hover:border-emerald-300/40 hover:text-emerald-200"
          >
            <Check size={13} />
            Mark all read
          </button>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="mb-6 flex flex-col gap-3 rounded-2xl border border-white/10 bg-[rgba(8,12,20,0.72)] p-3 shadow-glass backdrop-blur-2xl md:flex-row md:items-center md:justify-between"
      >
        <div className="flex items-center gap-2">
          <input
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Filter by graph, comment, or type..."
            className="h-10 rounded-lg border border-white/10 bg-white/[0.055] px-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-cyan-300/50 focus:bg-white/[0.075] focus:ring-4 focus:ring-cyan-300/10"
          />
        </div>
        <div className="flex items-center gap-2 overflow-x-auto">
          <FilterButton active={activeFilter === null} onClick={() => setActiveFilter(null)} label="All" />
          <FilterButton active={activeFilter === "mention"} onClick={() => setActiveFilter("mention")} label="Mentions" />
          <FilterButton active={activeFilter === "comment"} onClick={() => setActiveFilter("comment")} label="Comments" />
          <FilterButton active={activeFilter === "like"} onClick={() => setActiveFilter("like")} label="Likes" />
          <FilterButton active={activeFilter === "fork"} onClick={() => setActiveFilter("fork")} label="Forks" />
          <FilterButton active={activeFilter === "follow"} onClick={() => setActiveFilter("follow")} label="Followers" />
        </div>
      </motion.div>

      <div className="grid gap-6 xl:grid-cols-[1fr_320px]">
        <div>
          {filtered.length === 0 ? (
            <EmptyState
              icon={<Bell size={28} className="text-slate-500" />}
              title="No notifications"
              description="You&apos;re all caught up. We&apos;ll notify you when something new arrives."
            />
          ) : (
            <div className="space-y-6">
              {groupOrder.map((groupKey) => {
                const items = grouped[groupKey];
                if (items.length === 0) return null;
                return (
                  <NotificationGroup
                    key={groupKey}
                    groupKey={groupKey}
                    items={items}
                    onMarkRead={markRead}
                    onNavigate={(id) => markRead(id)}
                  />
                );
              })}
            </div>
          )}
          {isFetchingNextPage && (
            <div className="flex justify-center py-4">
              <Loader2 size={16} className="animate-spin text-cyan-300" />
            </div>
          )}
        </div>

        <aside className="space-y-6">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            className="rounded-2xl border border-white/10 bg-white/[0.03] p-4"
          >
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-slate-200">Inbox status</p>
              <span className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[11px] text-slate-300">
                {unreadCount} unread
              </span>
            </div>
            <div className="mt-4 space-y-2 text-xs text-muted-foreground">
              <div className="flex items-center justify-between rounded-lg border border-white/10 bg-white/[0.04] p-3">
                <span>Total notifications</span>
                <span className="font-mono text-slate-300">{notifications.length}</span>
              </div>
              <div className="flex items-center justify-between rounded-lg border border-white/10 bg-white/[0.04] p-3">
                <span>Read</span>
                <span className="font-mono text-emerald-300">{notifications.filter((n) => n.read).length}</span>
              </div>
              <div className="flex items-center justify-between rounded-lg border border-white/10 bg-white/[0.04] p-3">
                <span>Unread</span>
                <span className="font-mono text-amber-300">{unreadCount}</span>
              </div>
            </div>
          </motion.div>
        </aside>
      </div>
    </section>
  );
}

type FilterButtonProps = {
  active: boolean;
  onClick: () => void;
  label: string;
};

function FilterButton({ active, onClick, label }: FilterButtonProps) {
  return (
    <button
      onClick={onClick}
      className={
        active
          ? "shrink-0 rounded-lg border border-cyan-300/25 bg-cyan-300/12 px-3 py-2 text-xs text-cyan-100 transition"
          : "shrink-0 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-xs text-muted-foreground transition hover:bg-white/[0.075] hover:text-foreground"
      }
    >
      {label}
    </button>
  );
}
