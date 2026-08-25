import type { HTMLAttributes } from "react";
import { cn } from "../../lib/cn";

export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "rounded-2xl border border-white/12 bg-[rgba(9,13,20,0.68)] shadow-glass backdrop-blur-2xl",
        className
      )}
      {...props}
    />
  );
}
