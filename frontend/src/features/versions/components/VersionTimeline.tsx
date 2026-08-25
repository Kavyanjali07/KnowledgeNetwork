import { GitCommit, GitFork } from "lucide-react";
import { motion } from "framer-motion";
import { VersionCard } from "./VersionCard";
import type { VersionCommit } from "../types";

const itemVariants = {
  hidden: { opacity: 0, x: -24 },
  visible: (i: number) => ({
    opacity: 1,
    x: 0,
    transition: {
      delay: i * 0.06,
      duration: 0.45,
      ease: [0.16, 1, 0.3, 1]
    }
  })
};

function formatShortDate(value: string) {
  const date = new Date(value);
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

type TimelineProps = {
  commits: VersionCommit[];
  selectedCommitId: string | null;
  onSelectCommit: (id: string) => void;
  onRequestRestore: (id: string) => void;
  onRequestCompare: (id: string) => void;
};

const branchColor: Record<string, string> = {
  main: "bg-cyan-400 border-cyan-200",
  "hotfix/vector-search": "bg-amber-400 border-amber-200",
  "feature/pricing-model": "bg-violet-400 border-violet-200",
  "feature/ontology-cleanup": "bg-emerald-400 border-emerald-200",
  "experiment/ai-suggestions": "bg-fuchsia-400 border-fuchsia-200"
};

export function VersionTimeline({
  commits,
  selectedCommitId,
  onSelectCommit,
  onRequestRestore,
  onRequestCompare
}: TimelineProps) {
  return (
    <div className="relative pl-6">
      <div className="absolute inset-y-0 left-[11px] w-px bg-gradient-to-b from-white/20 via-white/12 to-white/4" />

      {commits.map((commit, index) => {
        const isSelected = selectedCommitId === commit.id;
        const accent = branchColor[commit.branch] ?? "bg-slate-400 border-slate-200";

        return (
          <motion.div
            key={commit.id}
            custom={index}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true, margin: "-40px" }}
            variants={itemVariants}
            onViewportEnter={() => onSelectCommit(commit.id)}
            className="relative mb-6"
          >
            <div className="absolute -left-6 top-[14px] flex items-center justify-center">
              <span
                className={[
                  "flex h-[18px] w-[18px] items-center justify-center rounded-full border-2 bg-[rgb(8,12,20)]",
                  isSelected ? "scale-110 ring-2 ring-cyan-300/50" : "",
                  accent
                ].join(" ")}
              >
                {commit.isMerge ? (
                  <GitFork size={9} className="text-slate-950" />
                ) : (
                  <GitCommit size={9} className="text-slate-950" />
                )}
              </span>
            </div>

            <VersionCard
              commit={commit}
              isSelected={isSelected}
              onRestore={() => onRequestRestore(commit.id)}
              onCompare={() => onRequestCompare(commit.id)}
            />

            <div className="mt-2 flex items-center gap-2 text-[11px] text-muted-foreground">
              <span className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 font-mono text-[10px] text-slate-300">
                {commit.hash}
              </span>
              <span className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5">
                {commit.branch}
              </span>
              <span>{formatShortDate(commit.timestamp)}</span>
            </div>
          </motion.div>
        );
      })}
    </div>
  );
}
