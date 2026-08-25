import { motion } from "framer-motion";
import { cn } from "../../../lib/cn";
import type { SocialUser, UserStatus } from "../types";

type AvatarSize = "xs" | "sm" | "md" | "lg";

type AvatarProps = {
  user: Pick<SocialUser, "initials" | "gradient" | "name">;
  size?: AvatarSize;
  status?: UserStatus;
  showStatus?: boolean;
  className?: string;
};

const sizeClasses: Record<AvatarSize, { container: string; text: string; ring: string; status: string }> = {
  xs: { container: "h-6 w-6", text: "text-[10px]", ring: "ring-[1.5px]", status: "h-1.5 w-1.5 border" },
  sm: { container: "h-8 w-8", text: "text-xs", ring: "ring-2", status: "h-2 w-2 border-[1.5px]" },
  md: { container: "h-10 w-10", text: "text-sm", ring: "ring-2", status: "h-2.5 w-2.5 border-2" },
  lg: { container: "h-14 w-14", text: "text-base", ring: "ring-[3px]", status: "h-3 w-3 border-2" }
};

const statusColors: Record<UserStatus, string> = {
  online: "bg-emerald-400 border-emerald-200/80",
  away: "bg-amber-400 border-amber-200/80",
  offline: "bg-slate-500 border-slate-400/60"
};

export function Avatar({ user, size = "md", status, showStatus = false, className }: AvatarProps) {
  const sizes = sizeClasses[size];

  return (
    <div className={cn("relative shrink-0", sizes.container, className)}>
      <motion.div
        whileHover={{ scale: 1.06 }}
        transition={{ type: "spring", stiffness: 400, damping: 20 }}
        className={cn(
          "flex h-full w-full items-center justify-center rounded-full bg-gradient-to-br font-semibold text-slate-950 shadow-glow",
          sizes.text,
          sizes.ring,
          "ring-white/20",
          user.gradient
        )}
        aria-label={user.name}
      >
        {user.initials}
      </motion.div>
      {showStatus && status && (
        <span
          className={cn(
            "absolute bottom-0 right-0 rounded-full",
            sizes.status,
            statusColors[status]
          )}
          aria-label={`Status: ${status}`}
        />
      )}
    </div>
  );
}
