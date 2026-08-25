import { motion } from "framer-motion";
import { WifiOff } from "lucide-react";
import { useEffect, useState } from "react";
import { cn } from "../../lib/cn";

type OfflineIndicatorProps = {
  className?: string;
};

export function OfflineIndicator({ className }: OfflineIndicatorProps) {
  const [offline, setOffline] = useState(false);

  useEffect(() => {
    const update = () => setOffline(!navigator.onLine);
    update();
    window.addEventListener("online", update);
    window.addEventListener("offline", update);
    return () => {
      window.removeEventListener("online", update);
      window.removeEventListener("offline", update);
    };
  }, []);

  if (!offline) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: -12 }}
      animate={{ opacity: 1, y: 0 }}
      className={cn(
        "flex items-center gap-2 border border-amber-400/30 bg-amber-400/10 px-4 py-2 text-xs text-amber-100",
        className
      )}
    >
      <WifiOff size={14} />
      You are currently offline. Some features may be limited.
    </motion.div>
  );
}
