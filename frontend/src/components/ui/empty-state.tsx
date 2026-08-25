import type { HTMLAttributes } from "react";
import { cn } from "../../lib/cn";

type EmptyStateProps = HTMLAttributes<HTMLDivElement> & {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
};

export function EmptyState({ icon, title, description, action, className, ...props }: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-2xl border border-white/10 bg-white/[0.03] p-10 text-center",
        className
      )}
      {...props}
    >
      {icon && <div className="text-slate-500">{icon}</div>}
      <h3 className="text-sm font-semibold text-slate-200">{title}</h3>
      {description && <p className="max-w-sm text-xs text-muted-foreground">{description}</p>}
      {action}
    </div>
  );
}
