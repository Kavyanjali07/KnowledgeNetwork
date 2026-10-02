import {
  Bell,
  ChevronDown,
  Command,
  LogOut,
  Moon,
  Plus,
  Search,
  Settings,
  Sparkles,
  Sun,
  User,
  Wand2
} from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useGlobalSearch } from "../../features/search/hooks/useGlobalSearch";
import { useTheme } from "../theme/ThemeProvider";
import { useAuth } from "../../lib/auth-context";
import { notificationApi } from "../../services";
import { NotificationCenterPanel } from "../../features/notifications/components/NotificationCenterPanel";

export function FloatingNavbar() {
  const navigate = useNavigate();
  const { currentUser, logout } = useAuth();
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);
  const { theme, setTheme } = useTheme();
  const { openSearch } = useGlobalSearch();

  const hasToken = typeof window !== "undefined" && Boolean(localStorage.getItem("auth_token"));
  const { data: unreadCount = 0 } = useQuery({
    queryKey: ["notifications-unread-count"],
    queryFn: () => notificationApi.getUnreadCount().then((r) => r.data),
    enabled: hasToken,
    retry: false,
    refetchInterval: hasToken ? 30000 : false
  });

  return (
    <header className="sticky top-4 z-30 mx-auto flex max-w-7xl items-center gap-2 rounded-xl border border-white/10 bg-[rgba(9,13,20,0.74)] px-2 py-2 shadow-glass backdrop-blur-2xl md:gap-3 md:px-3">
      <div className="hidden items-center gap-2 text-sm text-muted-foreground md:flex">
        <Link to="/explore" className="transition hover:text-foreground">Explore</Link>
        <span className="text-slate-600">/</span>
        <Link to="/graphs" className="transition hover:text-foreground">Networks</Link>
      </div>

      <button
        onClick={openSearch}
        className="flex h-10 min-w-0 flex-1 items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3 text-left text-sm text-muted-foreground transition hover:border-white/16 hover:bg-white/[0.07] md:gap-3"
      >
        <Search size={16} className="shrink-0 text-cyan-400" />
        <span className="truncate text-xs sm:text-sm">Search concepts, relationships, networks...</span>
        <span className="ml-auto hidden items-center gap-1 rounded-md border border-white/10 bg-black/20 px-1.5 py-0.5 font-mono text-[11px] text-slate-400 sm:flex">
          <Command size={11} />K
        </span>
      </button>

      <div className="flex items-center gap-1.5 lg:hidden">
        <button
          className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-white/[0.045] text-muted-foreground hover:bg-white/[0.075] hover:text-foreground"
          onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
          aria-label="Toggle theme"
        >
          {theme === "dark" ? <Moon size={18} /> : <Sun size={18} />}
        </button>
      </div>

      <div className="hidden items-center gap-2 lg:flex">
        <button onClick={() => navigate("/graphs")} className="inline-flex h-10 items-center gap-2 rounded-lg border border-cyan-500/30 bg-cyan-500/10 px-3.5 text-sm font-semibold text-cyan-300 transition hover:bg-cyan-500/20 hover:-translate-y-px">
          <Plus size={16} />
          New Network
        </button>
      </div>

      <div className="relative">
        <button
          className="relative inline-flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-white/[0.045] text-muted-foreground hover:bg-white/[0.075] hover:text-foreground"
          onClick={() => setNotificationsOpen((value) => !value)}
          aria-label="Open notifications"
        >
          <Bell size={16} />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full border-2 border-[rgb(8,12,20)] bg-rose-500 text-[10px] font-bold text-white">
              {unreadCount}
            </span>
          )}
        </button>
        <NotificationCenterPanel
          open={notificationsOpen}
          onClose={() => setNotificationsOpen(false)}
          onOpenHistory={() => {
            setNotificationsOpen(false);
            navigate("/notifications");
          }}
        />
      </div>



      {currentUser ? (
        <div className="relative">
          <button
            className="inline-flex h-10 items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-2 text-sm hover:bg-white/[0.075]"
            onClick={() => setUserOpen((value) => !value)}
          >
            <span className="inline-flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-slate-200 to-cyan-300 text-xs font-semibold text-slate-950">
              {(currentUser?.firstName?.[0] || currentUser?.email?.[0] || "U").toUpperCase()}
            </span>
            <ChevronDown size={14} className="hidden text-muted-foreground sm:block" />
          </button>
          {userOpen && (
            <div className="absolute right-0 top-12 w-60 rounded-xl border border-white/10 bg-popover p-2 shadow-glass backdrop-blur-2xl">
              <div className="border-b border-white/10 px-3 py-2">
                <p className="truncate text-sm font-semibold text-foreground">
                  {currentUser?.firstName ? `${currentUser.firstName} ${currentUser.lastName || ""}`.trim() : "Account"}
                </p>
                <p className="truncate text-xs text-muted-foreground">{currentUser?.email || ""}</p>
              </div>
              <div className="mt-1 space-y-0.5">
                <button onClick={() => { setUserOpen(false); navigate("/profile"); }} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm hover:bg-white/7">
                  <User size={15} /> Profile
                </button>
                <button onClick={() => { setUserOpen(false); navigate("/settings"); }} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm hover:bg-white/7">
                  <Settings size={15} /> Account settings
                </button>
                <button onClick={() => { setUserOpen(false); navigate("/notifications"); }} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm hover:bg-white/7">
                  <Bell size={15} /> Notifications
                </button>
                <button onClick={() => { setUserOpen(false); navigate("/graphs"); }} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-cyan-200 hover:bg-white/7">
                  <Sparkles size={15} /> Workspace library
                </button>
              </div>
              <div className="my-1 border-t border-white/10" />
              <button
                onClick={async () => {
                  setUserOpen(false);
                  await logout();
                }}
                className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-rose-400 hover:bg-rose-500/10 transition"
              >
                <LogOut size={15} /> Log out
              </button>
            </div>
          )}
        </div>
      ) : (
        <button
          onClick={() => navigate("/login")}
          className="inline-flex h-10 items-center gap-2 rounded-lg bg-cyan-500 px-3.5 text-xs font-semibold text-slate-950 transition hover:bg-cyan-400"
        >
          Sign In
        </button>
      )}
    </header>
  );
}


