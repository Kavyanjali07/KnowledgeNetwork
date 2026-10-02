import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { Network, X, ArrowRight, Sparkles, RefreshCw } from "lucide-react";

import { exploreApi, PublicRelatedNetwork } from "../../explore/api/exploreApi";
import { LicenseBadge } from "./LicenseBadge";

interface RelatedNetworksDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  networkId: string;
  networkTitle: string;
}

export function RelatedNetworksDrawer({
  isOpen,
  onClose,
  networkId,
  networkTitle
}: RelatedNetworksDrawerProps) {
  const [related, setRelated] = useState<PublicRelatedNetwork[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchRelated = async () => {
    if (!networkId) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await exploreApi.getRelatedPublicNetworks(networkId, 5);
      setRelated(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Failed to load related networks");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && networkId) {
      fetchRelated();
    }
  }, [isOpen, networkId]);

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm">
        <motion.div
          initial={{ x: "100%" }}
          animate={{ x: 0 }}
          exit={{ x: "100%" }}
          transition={{ type: "spring", damping: 25, stiffness: 200 }}
          className="w-full max-w-md h-full bg-[#0a0f1d] border-l border-cyan-500/20 p-6 flex flex-col justify-between shadow-2xl overflow-y-auto"
        >
          <div className="space-y-6">
            <div className="flex items-center justify-between border-b border-white/10 pb-4">
              <div className="flex items-center gap-2 text-cyan-300">
                <Sparkles size={20} />
                <h3 className="text-lg font-bold text-white">Related Networks</h3>
              </div>
              <button
                onClick={onClose}
                className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition"
              >
                <X size={20} />
              </button>
            </div>

            <p className="text-xs text-slate-400">
              Discovered public networks related to <span className="text-slate-200 font-semibold">&ldquo;{networkTitle}&rdquo;</span> based on shared concept structures and lineage.
            </p>

            {isLoading ? (
              <div className="flex min-h-[200px] items-center justify-center">
                <div className="h-7 w-7 animate-spin rounded-full border-2 border-cyan-400 border-t-transparent" />
              </div>
            ) : error ? (
              <div className="p-4 rounded-xl border border-red-500/20 bg-red-500/5 text-center space-y-2 text-xs">
                <p className="text-red-400">{error}</p>
                <button
                  onClick={fetchRelated}
                  className="px-3 py-1 rounded bg-red-500/20 text-red-200 hover:bg-red-500/30 transition inline-flex items-center gap-1"
                >
                  <RefreshCw size={12} /> Retry
                </button>
              </div>
            ) : related.length === 0 ? (
              <div className="p-8 rounded-2xl border border-white/10 bg-white/5 text-center space-y-2">
                <Network size={32} className="mx-auto text-slate-600" />
                <h4 className="text-sm font-semibold text-white">No Related Public Networks</h4>
                <p className="text-xs text-slate-400">
                  No other public networks currently share concept labels or lineage links with this network.
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {related.map((item) => (
                  <div
                    key={item.id}
                    className="p-4 rounded-xl border border-white/10 bg-white/5 hover:border-cyan-500/30 transition space-y-2"
                  >
                    <div className="flex items-center justify-between">
                      <span className="px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20 text-cyan-300 text-[10px] font-mono">
                        {item.relationReason}
                      </span>
                      <LicenseBadge licenseType={item.licenseType as any} />
                    </div>

                    <Link to={`/graphs/${item.id}`} onClick={onClose}>
                      <h4 className="text-sm font-bold text-white hover:text-cyan-300 transition">
                        {item.title}
                      </h4>
                    </Link>

                    {item.description && (
                      <p className="text-xs text-slate-400 line-clamp-2">{item.description}</p>
                    )}

                    <div className="flex items-center justify-between pt-1 text-xs">
                      <span className="text-slate-400">
                        By{" "}
                        <Link
                          to={`/creator/${encodeURIComponent(item.ownerUsername)}`}
                          onClick={onClose}
                          className="text-cyan-400 hover:underline font-medium"
                        >
                          @{item.ownerUsername}
                        </Link>
                      </span>

                      <Link
                        to={`/graphs/${item.id}`}
                        onClick={onClose}
                        className="inline-flex items-center gap-1 text-cyan-300 hover:text-white font-medium text-xs transition"
                      >
                        Traverse <ArrowRight size={12} />
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
