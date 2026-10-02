import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Globe, Loader2, X, AlertCircle } from "lucide-react";
import { LicenseSelector } from "./LicenseSelector";
import { Button } from "../../../components/ui/button";
import type { LicenseType } from "../../../services/graphApi";
import type { ManagedGraph } from "../../graphs/types";

interface PublishNetworkModalProps {
  isOpen: boolean;
  onClose: () => void;
  graph: ManagedGraph | null;
  onPublish: (licenseType: LicenseType, customAttribution?: string) => Promise<void>;
  isPending: boolean;
}

export function PublishNetworkModal({
  isOpen,
  onClose,
  graph,
  onPublish,
  isPending
}: PublishNetworkModalProps) {
  const [licenseType, setLicenseType] = useState<LicenseType>(graph?.licenseType || "CC_BY_4_0");
  const [customAttribution, setCustomAttribution] = useState(graph?.customAttribution || "");
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !graph) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await onPublish(licenseType, customAttribution.trim() || undefined);
      onClose();
    } catch (err: any) {
      setError(err.message || "Failed to publish Network.");
    }
  };

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-md">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.95 }}
          className="w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-2xl border border-white/10 bg-[rgba(10,15,28,0.97)] p-6 shadow-2xl backdrop-blur-2xl"
        >
          <div className="flex items-center justify-between border-b border-white/10 pb-4">
            <div className="flex items-center gap-2.5 text-cyan-300">
              <Globe size={20} />
              <h3 className="text-xl font-semibold text-slate-100">Publish Network</h3>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg p-1 text-muted-foreground hover:bg-white/10 hover:text-white"
            >
              <X size={20} />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="mt-4 space-y-4">
            {error && (
              <div className="flex items-center gap-2 rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-xs text-rose-300">
                <AlertCircle size={16} className="shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <div className="rounded-xl border border-cyan-400/20 bg-cyan-400/5 p-3.5 text-xs text-slate-200">
              <p className="font-semibold text-cyan-200 mb-1">
                You are about to publish &ldquo;{graph.title}&rdquo;
              </p>
              <p className="text-muted-foreground leading-relaxed">
                Publishing makes this connected body of knowledge publicly viewable. Visitors will be able to explore nodes, relationships, and derive new knowledge based on your selected license terms.
              </p>
            </div>

            <LicenseSelector
              value={licenseType}
              onChange={setLicenseType}
              customAttribution={customAttribution}
              onCustomAttributionChange={setCustomAttribution}
            />

            <div className="mt-6 flex justify-end gap-3 border-t border-white/10 pt-4">
              <Button type="button" variant="ghost" onClick={onClose}>
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isPending}
                className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-2"
              >
                {isPending && <Loader2 size={16} className="animate-spin" />}
                Publish Network Now
              </Button>
            </div>
          </form>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
