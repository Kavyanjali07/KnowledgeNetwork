import type { InputHTMLAttributes } from "react";
import { cn } from "../../lib/cn";

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  invalid?: boolean;
};

export function Input({ className, invalid, ...props }: InputProps) {
  return (
    <input
      className={cn(
        "h-11 w-full rounded-lg border bg-white/[0.055] px-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-cyan-300/50 focus:bg-white/[0.075] focus:ring-4 focus:ring-cyan-300/10",
        invalid ? "border-rose-400/55 focus:border-rose-300/70 focus:ring-rose-300/10" : "border-white/10",
        className
      )}
      {...props}
    />
  );
}
