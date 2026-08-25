import { RotateCcw, X } from "lucide-react";
import { motion } from "framer-motion";
import type { VersionEntryResponse } from "../../../services";

type RestoreModalProps = {
  commit: VersionEntryResponse | null;
  onClose: () => void;
  onConfirm: () => void;
};

export function RestoreModal({ commit, onClose, onConfirm }: RestoreModalProps) {
  if (!commit) return null;

  return (
    <div className="fixed inset-0 z-[110] flex items-center justify-center bg-black/70 px-4 backdrop-blur-xl">
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: -8 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.96, y: -8 }}
        className="w-full max-w-md rounded-2xl border border-white/12 bg-[rgba(8,12,22,0.96)] p-5 shadow-glass backdrop-blur-2xl"
      >
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm font-semibold text-slate-200">Restore version</p>
            <p className="mt-1 text-xs text-muted-foreground">
              You are about to restore the graph to commit <span className="font-mono text-slate-300">{commit.snapshot.id}</span>. Current
              uncommitted changes will be saved as a new draft version.
            </p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg border border-white/10 p-1.5 text-slate-400 transition hover:bg-white/10 hover:text-foreground"
          >
            <X size={14} />
          </button>
        </div>

        <div className="mt-4 rounded-xl border border-white/10 bg-white/[0.04] p-4">
          <p className="text-sm font-semibold text-slate-100">{commit.snapshot.name}</p>
          <div className="mt-2 flex items-center gap-3 text-[11px] text-muted-foreground">
            <span>{commit.createdBy}</span>
            <span>·</span>
            <span>{new Date(commit.createdAt).toLocaleString()}</span>
            <span>·</span>
            <span>v{commit.versionNumber}</span>
          </div>
        </div>

        <div className="mt-5 flex items-center justify-end gap-2">
          <button
            onClick={onClose}
            className="rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2 text-xs text-slate-300 transition hover:bg-white/[0.08]"
          >
            Cancel
          </button>
          <button
            onClick={() => {
              onConfirm();
            }}
            className="inline-flex items-center gap-2 rounded-lg border border-emerald-300/30 bg-emerald-400/10 px-3 py-2 text-xs text-emerald-200 transition hover:border-emerald-300/60 hover:bg-emerald-400/20"
          >
            <RotateCcw size={14} />
            Restore this version
          </button>
        </div>
      </motion.div>
    </div>
  );
}
