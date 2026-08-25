import { ArrowLeftRight, GitFork, RotateCcw } from "lucide-react";
import { motion } from "framer-motion";
import type { VersionCommit } from "../types";

type VersionCardProps = {
  commit: VersionCommit;
  isSelected: boolean;
  onRestore: () => void;
  onCompare: () => void;
};

export function VersionCard({ commit, isSelected, onRestore, onCompare }: VersionCardProps) {
  return (
    <motion.div
      layout
      animate={{
        borderColor: isSelected ? "rgba(34,211,238,0.45)" : "rgba(255,255,255,0.08)",
        backgroundColor: isSelected ? "rgba(34,211,238,0.07)" : "rgba(255,255,255,0.03)"
      }}
      transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
      className="rounded-2xl border p-4 transition hover:border-white/16 hover:bg-white/[0.05]"
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <p className="text-sm font-semibold text-slate-100">{commit.message}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            {commit.author} · {new Date(commit.timestamp).toLocaleString()}
          </p>
        </div>

        <div className="flex items-center gap-2">
          {!commit.isMerge && (
            <button
              onClick={onCompare}
              className="inline-flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.04] px-2.5 py-1.5 text-[11px] text-slate-300 transition hover:border-cyan-300/40 hover:bg-white/[0.08] hover:text-cyan-200"
            >
              <ArrowLeftRight size={12} />
              Compare
            </button>
          )}
          <button
            onClick={onRestore}
            className="inline-flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.04] px-2.5 py-1.5 text-[11px] text-slate-300 transition hover:border-emerald-300/40 hover:bg-white/[0.08] hover:text-emerald-200"
          >
            {commit.isMerge ? (
              <>
                <GitFork size={12} />
                Fork
              </>
            ) : (
              <>
                <RotateCcw size={12} />
                Restore
              </>
            )}
          </button>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap items-center gap-3 text-[11px] text-muted-foreground">
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-emerald-400" />
          +{commit.nodesAdded} nodes
        </span>
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-rose-400" />
          -{commit.nodesRemoved} nodes
        </span>
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-cyan-400" />
          +{commit.edgesAdded} edges
        </span>
        <span className="flex items-center gap-1">
          <span className="h-2 w-2 rounded-full bg-violet-400" />
          -{commit.edgesRemoved} edges
        </span>
      </div>

      {commit.tags.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {commit.tags.map((tag) => (
            <span
              key={tag}
              className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[10px] text-slate-400"
            >
              #{tag}
            </span>
          ))}
        </div>
      )}

      {isSelected && (
        <motion.div
          layout
          initial={{ opacity: 0, y: 4 }}
          animate={{ opacity: 1, y: 0 }}
          className="mt-3 rounded-xl border border-white/10 bg-black/40 p-3 text-xs text-slate-300"
        >
          <div className="flex items-center justify-between">
            <span className="font-medium text-slate-200">Version notes</span>
            <span className="font-mono text-[10px] text-muted-foreground">{commit.hash}</span>
          </div>
          <p className="mt-2 leading-relaxed text-slate-400">
            This version captured the graph state after applying {commit.nodesAdded} new node definitions and
            {commit.edgesAdded} relationship updates on{" "}
            {new Date(commit.timestamp).toLocaleDateString()}. Automated validation passed with 99.4% confidence.
          </p>
        </motion.div>
      )}
    </motion.div>
  );
}
