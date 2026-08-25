import { ArrowDownToLine, X } from "lucide-react";
import { motion } from "framer-motion";
import type { VersionEntryResponse } from "../../../services";

type CompareViewProps = {
  leftId: string | null;
  rightId: string | null;
  versionHistory: VersionEntryResponse[];
  onClose: () => void;
  onRestore: (id: string) => void;
};

export function CompareView({ leftId, rightId, versionHistory, onClose, onRestore }: CompareViewProps) {
  const left = leftId ? versionHistory.find((c) => c.id === leftId) ?? versionHistory[0] : versionHistory[0];
  const right = rightId ? versionHistory.find((c) => c.id === rightId) ?? versionHistory[versionHistory.length - 1] : versionHistory[versionHistory.length - 1];

  if (!left || !right) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 12 }}
      className="rounded-2xl border border-white/12 bg-[rgba(8,12,20,0.92)] p-4 shadow-glass backdrop-blur-2xl"
    >
      <div className="flex items-center justify-between border-b border-white/10 pb-3">
        <div>
          <p className="text-sm font-semibold text-slate-200">Compare versions</p>
          <p className="text-xs text-muted-foreground">
            {left.snapshot.id} vs {right.snapshot.id}
          </p>
        </div>
        <button
          onClick={onClose}
          className="rounded-lg border border-white/10 p-1.5 text-slate-400 transition hover:bg-white/10 hover:text-foreground"
        >
          <X size={14} />
        </button>
      </div>

      <div className="mt-4 grid gap-4 md:grid-cols-2">
        <VersionColumn commit={left} label="From" accent="from-violet-400" />
        <VersionColumn commit={right} label="To" accent="from-cyan-400" />
      </div>

      <div className="mt-4 flex justify-end">
        <button
          onClick={() => onRestore(right.id)}
          className="inline-flex items-center gap-2 rounded-lg border border-emerald-300/30 bg-emerald-400/10 px-3 py-2 text-xs text-emerald-200 transition hover:border-emerald-300/60 hover:bg-emerald-400/20"
        >
          <ArrowDownToLine size={14} />
          Restore this version
        </button>
      </div>
    </motion.div>
  );
}

function VersionColumn({ commit, label, accent }: { commit: VersionEntryResponse; label: string; accent: string }) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/[0.03] p-4">
      <div className={`mb-2 h-1.5 rounded-full bg-gradient-to-r ${accent}`} />
      <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">{label}</p>
      <p className="mt-1 text-sm font-semibold text-slate-100">{commit.snapshot.name}</p>
      <p className="mt-1 text-xs text-muted-foreground">
        {commit.createdBy} · v{commit.versionNumber}
      </p>
      <p className="mt-2 font-mono text-[10px] text-slate-500">{commit.snapshot.id}</p>
    </div>
  );
}
