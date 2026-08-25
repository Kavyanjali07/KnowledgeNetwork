import { motion } from "framer-motion";
import { achievements } from "../data/profile";

const cardVariants = {
  hidden: { opacity: 0, y: 14 },
  visible: (i: number) => ({
    opacity: 1,
    y: 0,
    transition: { delay: i * 0.06, duration: 0.4, ease: [0.16, 1, 0.3, 1] }
  })
};

export function AchievementsGrid() {
  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {achievements.map((achievement, index) => (
        <motion.div
          key={achievement.id}
          custom={index}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-40px" }}
          variants={cardVariants}
          className={`rounded-2xl border p-4 ${
            achievement.unlocked
              ? "border-white/10 bg-white/[0.04]"
              : "border-white/6 bg-white/[0.015] opacity-75"
          }`}
        >
          <div className="flex items-center gap-3">
            <span className="flex h-12 w-12 items-center justify-center rounded-xl border border-white/10 bg-white/[0.04] text-xl">
              {achievement.icon}
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-slate-100">{achievement.title}</p>
              <p className="text-xs text-muted-foreground">{achievement.description}</p>
            </div>
          </div>
          <div className="mt-3 flex items-center justify-between">
            <span
              className={`rounded-full border px-2 py-0.5 text-[10px] ${
                achievement.unlocked
                  ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-200"
                  : "border-white/10 bg-white/[0.04] text-slate-400"
              }`}
            >
              {achievement.unlocked ? `Unlocked ${achievement.unlockedAt}` : "Locked"}
            </span>
            {achievement.unlocked && (
              <span className="text-[10px] text-emerald-300">✓</span>
            )}
          </div>
        </motion.div>
      ))}
    </div>
  );
}
