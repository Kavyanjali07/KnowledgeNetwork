import { Activity, Bell, History, LayoutDashboard, Network, Search, Settings2, User } from "lucide-react";
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

export function BottomNav() {
  const location = useLocation();

  return (
    <nav className={cn("fixed inset-x-0 bottom-0 z-40 border-t border-white/10 bg-[rgba(8,12,20,0.92)] backdrop-blur-2xl md:hidden")}>
      <div className="flex items-center justify-between px-2 pb-[max(0.5rem,env(safe-area-inset-bottom))]">
        {navItems.map((item) => {
          const active = location.pathname === item.href || (item.href === "/graphs" && location.pathname.startsWith("/graph"));
          return (
            <Link
              key={item.label}
              to={item.href}
              className={cn(
                "flex h-14 min-w-0 flex-1 flex-col items-center justify-center gap-0.5 text-[10px] transition",
                active ? "text-cyan-300" : "text-muted-foreground"
              )}
            >
              <item.icon size={18} className={cn(active && "scale-110")} />
              <span className="truncate">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

