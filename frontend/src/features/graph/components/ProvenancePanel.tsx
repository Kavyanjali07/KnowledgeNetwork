import { Link } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { GitFork, X, ExternalLink, CheckCircle2, Lock } from "lucide-react";
import { useGraphProvenance } from "../../../hooks/use-queries";

import { LicenseBadge } from "./LicenseBadge";
import { Button } from "../../../components/ui/button";

interface ProvenancePanelProps {
  isOpen: boolean;
  onClose: () => void;
  workspaceId: string;
  networkTitle: string;
  ownerName?: string;
}

export function ProvenancePanel({
  isOpen,
  onClose,
  workspaceId,
  networkTitle,
  ownerName = "Unknown Creator"
}: ProvenancePanelProps) {
  const { data: provenance, isLoading, isError } = useGraphProvenance(workspaceId);

  if (!isOpen) return null;

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
            <div className="flex items-center gap-2 text-cyan-300">
              <GitFork size={20} />
              <h3 className="text-xl font-semibold text-slate-100">Knowledge Provenance & Lineage</h3>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg p-1 text-muted-foreground hover:bg-white/10 hover:text-white"
            >
              <X size={20} />
            </button>
          </div>

          <div className="mt-4 space-y-4 text-xs text-slate-200">
            <div className="rounded-xl border border-cyan-400/20 bg-cyan-400/5 p-4 space-y-1">
              <p className="font-semibold text-cyan-200 text-sm">{networkTitle}</p>
              <p className="text-muted-foreground text-xs">
                Current Owner: <span className="text-slate-100 font-medium">{ownerName}</span>
              </p>
            </div>

            {isLoading ? (
              <div className="flex min-h-[120px] items-center justify-center">
                <div className="h-6 w-6 animate-spin rounded-full border-2 border-cyan-300 border-t-transparent" />
              </div>
            ) : isError || !provenance || !provenance.isDerivative ? (
              <div className="rounded-xl border border-white/10 bg-white/[0.02] p-4 text-center space-y-2">
                <CheckCircle2 size={24} className="mx-auto text-emerald-400" />
                <p className="font-semibold text-slate-100 text-sm">Original Knowledge Network</p>
                <p className="text-muted-foreground text-xs">
                  This network is an original work created on KnowledgeNetwork. It was not derived from any prior published network.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                <div className="rounded-xl border border-violet-500/30 bg-violet-500/10 p-4 space-y-3">
                  <div className="flex items-center gap-2 text-violet-300 font-semibold text-xs uppercase tracking-wider">
                    <GitFork size={15} />
                    <span>DERIVED FROM (Parent Source Network)</span>
                  </div>

                  <div className="space-y-2 pt-1 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Original Source Work:</span>
                      {provenance.sourceWorkspaceId ? (
                        <Link
                          to={`/graphs/${provenance.sourceWorkspaceId}`}
                          onClick={onClose}
                          className="font-semibold text-cyan-300 hover:underline flex items-center gap-1"
                        >
                          {provenance.name} <ExternalLink size={12} />
                        </Link>
                      ) : (
                        <span className="font-medium text-amber-300 flex items-center gap-1">
                          <Lock size={12} /> Private Network (Access Restricted)
                        </span>
                      )}
                    </div>


                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Original Creator:</span>
                      <span className="font-medium text-cyan-200">{provenance.originalCreatorName || "Community Member"}</span>
                    </div>

                    {provenance.sourceLicense && (
                      <div className="flex items-center justify-between">
                        <span className="text-slate-400">Source License:</span>
                        <LicenseBadge licenseType={provenance.sourceLicense} />
                      </div>
                    )}

                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Derivation Date:</span>
                      <span className="text-slate-300">
                        {new Date(provenance.createdAt).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "short",
                          day: "numeric"
                        })}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="rounded-xl border border-white/10 bg-white/[0.03] p-3 text-[11px] leading-relaxed text-slate-300 space-y-1">
                  <p className="font-medium text-slate-100">Attribution Notice</p>
                  <p className="text-muted-foreground">
                    This derived network maintains legal provenance linking back to original creator &ldquo;{provenance.originalCreatorName}&rdquo; under {provenance.sourceLicense || "CC BY 4.0"} terms.
                  </p>
                </div>
              </div>
            )}

            <div className="mt-6 flex justify-end border-t border-white/10 pt-4">
              <Button variant="ghost" onClick={onClose}>
                Close
              </Button>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
