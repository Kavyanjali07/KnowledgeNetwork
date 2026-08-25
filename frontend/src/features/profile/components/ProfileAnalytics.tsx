import { memo } from "react";
import { motion } from "framer-motion";
import type { ProfileStat } from "../types";

type StatCardProps = {
  stat: ProfileStat;
  index: number;
};

const StatCard = memo(function StatCard({ stat, index }: StatCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.08, duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
      className="rounded-2xl border border-white/10 bg-white/[0.04] p-5 shadow-glass backdrop-blur-xl"
    >
      <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{stat.label}</p>
      <p className="mt-3 text-3xl font-semibold tracking-tight text-slate-100">{stat.value}</p>
      <div className={`mt-3 h-1.5 w-full rounded-full bg-gradient-to-r ${stat.accent} opacity-80`} />
      <p className="mt-2 text-xs text-muted-foreground">{stat.change}</p>
    </motion.div>
  );
});

export function ProfileAnalytics({ stats }: { stats: ProfileStat[] }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {stats.map((stat, index) => (
        <StatCard key={stat.label} stat={stat} index={index} />
      ))}
    </div>
  );
}
