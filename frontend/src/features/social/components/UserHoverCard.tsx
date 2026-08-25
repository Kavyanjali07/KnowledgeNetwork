import { AnimatePresence, motion } from "framer-motion";
import { useCallback, useRef, useState } from "react";
import { cn } from "../../../lib/cn";
import type { SocialUser } from "../types";
import { formatCompactNumber } from "../utils/time";
import { Avatar } from "./Avatar";

function createFallbackUser(userId: string): SocialUser {
  return {
    id: userId,
    name: userId,
    username: userId,
    initials: userId.slice(0, 2).toUpperCase(),
    gradient: "from-slate-400 to-slate-500",
    bio: "",
    status: "offline",
    stats: { posts: 0, followers: 0, following: 0 }
  };
}

type UserHoverCardProps = {
  user: SocialUser;
  children: React.ReactNode;
  className?: string;
};

export function UserHoverCard({ user, children, className }: UserHoverCardProps) {
  const [open, setOpen] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const show = useCallback(() => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setOpen(true), 350);
  }, []);

  const hide = useCallback(() => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setOpen(false), 150);
  }, []);

  const keepOpen = useCallback(() => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    setOpen(true);
  }, []);

  return (
    <div
      ref={containerRef}
      className={cn("relative inline-flex", className)}
      onMouseEnter={show}
      onMouseLeave={hide}
      onFocus={show}
      onBlur={hide}
    >
      {children}
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 6, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 4, scale: 0.97 }}
            transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
            className="absolute left-0 top-full z-50 mt-2 w-72 origin-top-left"
            onMouseEnter={keepOpen}
            onMouseLeave={hide}
          >
            <div className="overflow-hidden rounded-xl border border-white/12 bg-[rgba(9,13,20,0.95)] p-4 shadow-glass backdrop-blur-2xl">
              <div className="flex items-start gap-3">
                <Avatar user={user} size="lg" status={user.status} showStatus />
                <div className="min-w-0 flex-1">
                  <p className="truncate font-semibold">{user.name}</p>
                  <p className="text-sm text-cyan-200/90">@{user.username}</p>
                </div>
              </div>
              <p className="mt-3 text-sm leading-5 text-muted-foreground">{user.bio}</p>
              <div className="mt-4 grid grid-cols-3 gap-2 border-t border-white/10 pt-3 text-center">
                <div>
                  <p className="text-sm font-semibold">{formatCompactNumber(user.stats.posts)}</p>
                  <p className="text-xs text-muted-foreground">Posts</p>
                </div>
                <div>
                  <p className="text-sm font-semibold">{formatCompactNumber(user.stats.followers)}</p>
                  <p className="text-xs text-muted-foreground">Followers</p>
                </div>
                <div>
                  <p className="text-sm font-semibold">{formatCompactNumber(user.stats.following)}</p>
                  <p className="text-xs text-muted-foreground">Following</p>
                </div>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

type UserAvatarLinkProps = {
  userId: string;
  size?: "xs" | "sm" | "md" | "lg";
  showStatus?: boolean;
};

export function UserAvatarLink({ userId, size = "md", showStatus = true }: UserAvatarLinkProps) {
  const user = createFallbackUser(userId);

  return (
    <UserHoverCard user={user}>
      <button type="button" className="rounded-full outline-none ring-cyan-300/0 transition focus-visible:ring-2 focus-visible:ring-cyan-300/50">
        <Avatar user={user} size={size} status={user.status} showStatus={showStatus} />
      </button>
    </UserHoverCard>
  );
}
