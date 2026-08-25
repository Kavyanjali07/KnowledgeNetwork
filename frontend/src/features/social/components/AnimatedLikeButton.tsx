import { AnimatePresence, motion } from "framer-motion";
import { Heart } from "lucide-react";
import { useCallback, useState } from "react";
import { cn } from "../../../lib/cn";

type AnimatedLikeButtonProps = {
  liked: boolean;
  count: number;
  onToggle: () => void;
  size?: "sm" | "md";
};

const burstParticles = Array.from({ length: 8 }, (_, index) => ({
  id: index,
  angle: (index / 8) * 360,
  distance: 18 + (index % 3) * 6
}));

export function AnimatedLikeButton({ liked, count, onToggle, size = "md" }: AnimatedLikeButtonProps) {
  const [burst, setBurst] = useState(false);

  const handleClick = useCallback(() => {
    if (!liked) {
      setBurst(true);
      setTimeout(() => setBurst(false), 600);
    }
    onToggle();
  }, [liked, onToggle]);

  const iconSize = size === "sm" ? 15 : 18;

  return (
    <button
      type="button"
      onClick={handleClick}
      className={cn(
        "group relative inline-flex items-center gap-1.5 rounded-lg px-2 py-1.5 text-sm transition",
        liked
          ? "text-rose-300 hover:bg-rose-300/10"
          : "text-muted-foreground hover:bg-white/6 hover:text-rose-200"
      )}
      aria-pressed={liked}
      aria-label={liked ? "Unlike" : "Like"}
    >
      <span className="relative flex items-center justify-center">
        <AnimatePresence>
          {burst &&
            burstParticles.map((particle) => (
              <motion.span
                key={particle.id}
                className="pointer-events-none absolute h-1.5 w-1.5 rounded-full bg-rose-400"
                initial={{ opacity: 1, scale: 1, x: 0, y: 0 }}
                animate={{
                  opacity: 0,
                  scale: 0,
                  x: Math.cos((particle.angle * Math.PI) / 180) * particle.distance,
                  y: Math.sin((particle.angle * Math.PI) / 180) * particle.distance
                }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
              />
            ))}
        </AnimatePresence>
        <motion.span
          animate={liked ? { scale: [1, 1.35, 1] } : { scale: 1 }}
          transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
        >
          <Heart
            size={iconSize}
            className={cn("transition", liked ? "fill-rose-400 text-rose-400" : "group-hover:text-rose-300")}
          />
        </motion.span>
      </span>
      {count > 0 && <span className="tabular-nums">{count}</span>}
    </button>
  );
}
