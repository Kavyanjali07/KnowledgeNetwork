import {
  Activity,
  Bell,
  ChevronLeft,
  History,
  LayoutDashboard,
  Network,
  PanelLeftClose,
  PanelLeftOpen,
  Search,
  Settings2,
  User
} from "lucide-react";
import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { cn } from "../../lib/cn";

const navItems = [
  { label: "Dashboard", icon: LayoutDashboard, href: "/dashboard" },
  { label: "Graphs", icon: Network, href: "/graphs" },
  { label: "History", icon: History, href: "/history" },
  { label: "Search", icon: Search, href: "/search" },
  { label: "Notifications", icon: Bell, href: "/notifications" },
  { label: "Activity", icon: Activity, href: "/activity" },
  { label: "Profile", icon: User, href: "/profile" },
  { label: "Settings", icon: Settings2, href: "/settings" }
];

export function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  return (
    <>
      <button
        className="fixed left-4 top-4 z-40 inline-flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-white/8 text-slate-100 backdrop-blur-xl md:hidden"
        onClick={() => setMobileOpen(true)}
        aria-label="Open sidebar"
      >
        <PanelLeftOpen size={18} />
      </button>

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 border-r border-white/10 bg-[rgba(8,12,20,0.82)] p-3 shadow-glass backdrop-blur-2xl transition-transform duration-200 md:sticky md:translate-x-0",
          collapsed ? "md:w-[76px]" : "md:w-[272px]",
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex h-full flex-col">
          <div className="flex items-center gap-3 px-2 py-2">
            <Link to="/dashboard" className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-violet-500 to-cyan-400 text-white shadow-glow">
              <Network size={18} />
            </Link>
            {!collapsed && (
              <Link to="/dashboard" className="min-w-0">
                <p className="truncate text-sm font-semibold">KnowledgeNetwork</p>
                <p className="truncate text-xs text-muted-foreground">Research Graph</p>
              </Link>
            )}
            <button
              className="ml-auto hidden h-8 w-8 items-center justify-center rounded-md text-muted-foreground hover:bg-white/8 hover:text-foreground md:inline-flex"
              onClick={() => setCollapsed((value) => !value)}
              aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            >
              {collapsed ? <PanelLeftOpen size={16} /> : <PanelLeftClose size={16} />}
            </button>
            <button
              className="ml-auto inline-flex h-8 w-8 items-center justify-center rounded-md text-muted-foreground hover:bg-white/8 hover:text-foreground md:hidden"
              onClick={() => setMobileOpen(false)}
              aria-label="Close sidebar"
            >
              <ChevronLeft size={16} />
            </button>
          </div>

          <div className="mt-4 rounded-lg border border-white/10 bg-white/[0.04] p-2">
            {!collapsed && <p className="px-2 pb-2 text-xs font-medium text-muted-foreground">Workspace</p>}
            <Link to="/graphs" className="flex w-full items-center gap-3 rounded-md px-2 py-2 text-left hover:bg-white/8">
              <div className="h-7 w-7 rounded-md bg-cyan-400/20 ring-1 ring-cyan-300/30 flex items-center justify-center text-cyan-300">
                <Network size={14} />
              </div>
              {!collapsed && (
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">Product Intelligence</p>
                  <p className="truncate text-xs text-muted-foreground">42 nodes synced</p>
                </div>
              )}
            </Link>
          </div>

          <nav className="mt-4 space-y-1">
            {navItems.map((item) => {
              const active = location.pathname === item.href || (item.href === "/graphs" && location.pathname.startsWith("/graph"));
              return (
                <Link
                  key={item.label}
                  to={item.href}
                  onClick={() => setMobileOpen(false)}
                  className={cn(
                    "group flex h-10 w-full items-center gap-3 rounded-lg px-3 text-sm transition",
                    active
                      ? "bg-white/10 text-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.08)]"
                      : "text-muted-foreground hover:bg-white/7 hover:text-foreground"
                  )}
                >
                  <item.icon
                    size={17}
                    className={cn(active ? "text-cyan-300" : "text-slate-500 group-hover:text-slate-200")}
                  />
                  {!collapsed && <span>{item.label}</span>}
                </Link>
              );
            })}
          </nav>

          <div className="mt-auto rounded-lg border border-white/10 bg-white/[0.035] p-3">
            {!collapsed ? (
              <>
                <p className="text-xs font-medium text-muted-foreground">Storage</p>
                <div className="mt-3 h-1.5 rounded-full bg-white/8">
                  <div className="h-full w-2/3 rounded-full bg-gradient-to-r from-violet-400 to-cyan-300" />
                </div>
                <p className="mt-2 text-xs text-muted-foreground">68% indexed</p>
              </>
            ) : (
              <div className="mx-auto h-8 w-8 rounded-full border border-cyan-300/30 bg-cyan-300/10" />
            )}
          </div>
        </div>
      </aside>

      {mobileOpen && (
        <button
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm md:hidden"
          onClick={() => setMobileOpen(false)}
          aria-label="Close sidebar overlay"
        />
      )}
    </>
  );
}

