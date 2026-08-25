import { motion } from "framer-motion";
import type { NotificationItem, NotificationGroupKey } from "../types";
import { Avatar } from "../../social/components/Avatar";
import {
  Heart,
  MessageCircle,
  GitFork,
  UserPlus,
  AtSign,
  Route,
  Check
} from "lucide-react";

const typeConfig: Record<string, { icon: typeof Heart; label: string; accent: string }> = {
  like: { icon: Heart, label: "liked", accent: "text-rose-300" },
  comment: { icon: MessageCircle, label: "commented on", accent: "text-cyan-300" },
  fork: { icon: GitFork, label: "forked", accent: "text-violet-300" },
  follow: { icon: UserPlus, label: "followed you", accent: "text-emerald-300" },
  mention: { icon: AtSign, label: "mentioned you in", accent: "text-amber-300" },
  system: { icon: Route, label: "updated", accent: "text-slate-300" }
} as const;

function timeAgo(value: string) {
  const date = new Date(value);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const seconds = Math.floor(diffMs / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) return `${days}d ago`;
  if (hours > 0) return `${hours}h ago`;
  if (minutes > 0) return `${minutes}m ago`;
  return "Just now";
}

type Props = {
  groupKey: NotificationGroupKey;
  items: NotificationItem[];
  onMarkRead: (id: string) => void;
  onNavigate: (id: string) => void;
};

function groupLabel(key: NotificationGroupKey) {
  switch (key) {
    case "today":
      return "Today";
    case "yesterday":
      return "Yesterday";
    case "thisWeek":
      return "This Week";
    default:
      return "Older";
  }
}

export function NotificationGroup({ groupKey, items, onMarkRead, onNavigate }: Props) {
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between px-3">
        <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">{groupLabel(groupKey)}</span>
        {items.some((n) => !n.read) && (
          <span className="rounded-full border border-cyan-500/30 bg-cyan-500/10 px-2 py-0.5 text-[10px] text-cyan-200">
            {items.filter((n) => !n.read).length} new
          </span>
        )}
      </div>
      <div className="space-y-1.5">
        {items.map((notification) => (
          <NotificationRow
            key={notification.id}
            notification={notification}
            onMarkRead={onMarkRead}
            onNavigate={onNavigate}
          />
        ))}
      </div>
    </div>
  );
}

type RowProps = {
  notification: NotificationItem;
  onMarkRead: (id: string) => void;
  onNavigate: (id: string) => void;
};

function NotificationRow({ notification, onMarkRead, onNavigate }: RowProps) {
  const config = typeConfig[notification.type] ?? typeConfig.system;
  if (!config) return null;

  const Icon = config.icon;
  const label = config.label;
  const accent = config.accent;

  return (
    <motion.div layout initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="relative">
      <div
        className={`group flex items-start gap-3 rounded-xl border p-3 transition ${
          notification.read
            ? "border-white/8 bg-white/[0.02]"
            : "border-cyan-500/20 bg-cyan-500/[0.06] shadow-glow"
        }`}
      >
        <div className="relative">
          <Avatar user={{ initials: "U", gradient: "from-slate-400 to-slate-600", name: "User" }} size="sm" />
          <span className="absolute -bottom-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full border-2 border-[rgb(8,12,22)] bg-[rgb(8,12,20)]">
            <Icon size={10} className={accent} />
          </span>
        </div>

        <div className="min-w-0 flex-1">
          <p className="text-xs text-slate-300">
            <span className="font-semibold text-slate-100">User</span> {label}{" "}
            <span className="font-medium text-foreground">{notification.entityName}</span>
          </p>
          {notification.commentPreview && (
            <p className="mt-1 line-clamp-2 text-[11px] text-muted-foreground">"{notification.commentPreview}"</p>
          )}
          <p className="mt-1 text-[10px] text-muted-foreground">{timeAgo(notification.timestamp)}</p>
        </div>

        <div className="flex items-center gap-1">
          {!notification.read && (
            <button
              onClick={() => onMarkRead(notification.id)}
              className="rounded-lg border border-white/10 p-1 text-slate-400 opacity-0 transition hover:border-emerald-300/40 hover:text-emerald-200 group-hover:opacity-100"
              title="Mark as read"
            >
              <Check size={12} />
            </button>
          )}
          <button
            onClick={() => onNavigate(notification.id)}
            className="rounded-lg border border-white/10 p-1 text-slate-400 opacity-0 transition hover:border-cyan-300/40 hover:text-cyan-200 group-hover:opacity-100"
            title="Open"
          >
            <Route size={12} />
          </button>
        </div>
      </div>
    </motion.div>
  );
}
