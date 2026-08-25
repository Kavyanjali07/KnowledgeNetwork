import { motion } from "framer-motion";
import { Plus } from "lucide-react";
import { cn } from "../../lib/cn";

type FloatingActionButtonProps = {
  children?: React.ReactNode;
  className?: string;
  onClick?: () => void;
  ariaLabel?: string;
  expanded?: boolean;
  label?: string;
};

export function FloatingActionButton({
  children,
  className,
  onClick,
  ariaLabel,
  expanded = false,
  label = "Create"
}: FloatingActionButtonProps) {
  return (
    <motion.button
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.92 }}
      onClick={onClick}
      aria-label={ariaLabel}
      className={cn(
        "relative inline-flex items-center gap-2 overflow-hidden rounded-full border border-white/10 bg-gradient-to-r from-violet-500 to-cyan-400 px-4 py-3 text-sm font-medium text-white shadow-glow transition",
        expanded ? "w-auto" : "h-12 w-12 justify-center",
        className
      )}
    >
      <span className="flex h-5 w-5 items-center justify-center">
        <Plus size={18} />
      </span>
      {expanded && <span>{label}</span>}
      {children}
    </motion.button>
  );
}
