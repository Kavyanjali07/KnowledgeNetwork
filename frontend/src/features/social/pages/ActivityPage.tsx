import { motion } from "framer-motion";
import { Activity, Loader2, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { SocialFeed } from "../components/SocialFeed";

export function ActivityPage() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = window.setTimeout(() => setLoading(false), 450);
    return () => window.clearTimeout(timer);
  }, []);

  return (
    <section className="mx-auto mt-6 max-w-2xl pt-4">
      <motion.div
        className="mb-6 flex flex-wrap items-end justify-between gap-4"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-muted-foreground">Team activity</p>
          <h1 className="text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Workspace feed</h1>
          <p className="mt-3 max-w-xl text-sm leading-6 text-muted-foreground">
            Collaborate on graph updates, mention teammates, react to insights, and follow threaded discussions.
          </p>
        </div>
        <div className="flex items-center gap-2 rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1.5 text-xs text-cyan-100">
          <Activity size={13} />
          Live
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05, duration: 0.28 }}
        className="mb-4 flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-3 text-xs sm:text-sm text-muted-foreground md:px-4"
      >
        <Sparkles size={14} className="text-violet-300 sm:hidden" />
        <Sparkles size={16} className="text-violet-300 hidden sm:block" />
        Try @mentions, emoji reactions, nested replies, and hover over avatars for profiles.
      </motion.div>

      {loading ? (
        <div className="flex items-center justify-center py-12 sm:py-20">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-2 text-sm text-cyan-300"
          >
            <Loader2 size={18} className="animate-spin" />
            Loading activity...
          </motion.div>
        </div>
      ) : (
        <SocialFeed />
      )}
    </section>
  );
}
