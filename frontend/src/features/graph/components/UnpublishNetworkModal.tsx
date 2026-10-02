import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Lock, Loader2, AlertTriangle } from "lucide-react";

import { Button } from "../../../components/ui/button";
import type { ManagedGraph } from "../../graphs/types";

interface UnpublishNetworkModalProps {
  isOpen: boolean;
  onClose: () => void;
  graph: ManagedGraph | null;
  onUnpublish: () => Promise<void>;
  isPending: boolean;
}

export function UnpublishNetworkModal({
  isOpen,
  onClose,
  graph,
  onUnpublish,
  isPending
}: UnpublishNetworkModalProps) {
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !graph) return null;

  const handleConfirm = async () => {
    setError(null);
    try {
      await onUnpublish();
      onClose();
    } catch (err: any) {
      setError(err.message || "Failed to unpublish Network.");
    }
  };

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-md">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.95 }}
          className="w-full max-w-md overflow-hidden rounded-2xl border border-amber-500/30 bg-[rgba(16,12,10,0.97)] p-6 shadow-2xl backdrop-blur-2xl"
        >
          <div className="flex items-center gap-2.5 text-amber-400 border-b border-white/10 pb-4">
            <Lock size={20} />
            <h3 className="text-xl font-semibold text-slate-100">Unpublish Network</h3>
          </div>

          <div className="mt-4 space-y-3 text-xs text-slate-300">
            {error && (
              <div className="flex items-center gap-2 rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-xs text-rose-300">
                <AlertTriangle size={16} className="shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <p>
              Are you sure you want to unpublish <span className="font-semibold text-white">&ldquo;{graph.title}&rdquo;</span>?
            </p>
            <p className="text-muted-foreground leading-relaxed">
              Unpublishing will return this Network to <span className="font-medium text-amber-300">Private</span> status. Non-members will no longer be able to explore or view this network canvas via public links.
            </p>
          </div>

          <div className="mt-6 flex justify-end gap-3 border-t border-white/10 pt-4">
            <Button type="button" variant="ghost" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="button"
              disabled={isPending}
              onClick={handleConfirm}
              className="bg-amber-600 text-white font-semibold hover:bg-amber-500 gap-2"
            >
              {isPending && <Loader2 size={16} className="animate-spin" />}
              Unpublish Network
            </Button>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
