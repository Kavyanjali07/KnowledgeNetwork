import type { ButtonHTMLAttributes } from "react";
import { cn } from "../../lib/cn";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variants: Record<ButtonVariant, string> = {
  primary:
    "bg-gradient-to-r from-violet-500 via-fuchsia-500 to-cyan-400 text-white shadow-glow hover:-translate-y-0.5 hover:brightness-110",
  secondary:
    "border border-white/10 bg-white/[0.055] text-foreground hover:-translate-y-0.5 hover:bg-white/[0.085]",
  ghost: "text-muted-foreground hover:bg-white/[0.07] hover:text-foreground"
};

export function Button({ className, variant = "primary", type = "button", ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        "inline-flex h-11 items-center justify-center gap-2 rounded-lg px-4 text-sm font-medium transition duration-200 ease-out disabled:pointer-events-none disabled:opacity-50",
        variants[variant],
        className
      )}
      {...props}
    />
  );
}
