import { useState } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Globe, GitFork, Shield, Share2, Info, Lock, CheckCircle2, Sparkles, ArrowLeft, User } from "lucide-react";
import { Button } from "../../../components/ui/button";
import { LicenseBadge } from "./LicenseBadge";
import { LICENSE_METADATA, type GraphResponse } from "../../../services/graphApi";
import { useAuth } from "../../../lib/auth-context";
import { useToast } from "../../../components/ui/toast";

interface PublicNetworkHeaderProps {
  graph: GraphResponse;
  isOwnerOrEditor: boolean;
  isReadOnly?: boolean;
  fromConcept?: string;
  onOpenProvenance: () => void;
  onOpenRelated: () => void;
  onFork: () => void;
  onOpenPublishModal: () => void;
  onOpenUnpublishModal: () => void;
}

export function PublicNetworkHeader({
  graph,
  isOwnerOrEditor,
  fromConcept,
  onOpenProvenance,
  onOpenRelated,
  onFork,
  onOpenPublishModal,
  onOpenUnpublishModal
}: PublicNetworkHeaderProps) {
  const { token } = useAuth();
  const { addToast } = useToast();
  const [copied, setCopied] = useState(false);

  const license = graph.licenseType || "CC_BY_4_0";
  const licenseMeta = LICENSE_METADATA[license] || LICENSE_METADATA.CC_BY_4_0;
  const isPublished = graph.isPublished || graph.visibility === "PUBLIC";
  const allowsDerivatives = licenseMeta.allowsDerivatives;


  const handleShare = () => {
    const url = window.location.href;
    navigator.clipboard?.writeText(url);
    setCopied(true);
    addToast({ type: "info", title: "Link copied", description: "Public network link copied to clipboard." });
    setTimeout(() => setCopied(false), 2000);
  };

  const handleForkClick = () => {
    if (!token) {
      addToast({ type: "info", title: "Sign in required", description: "Please log in or register to derive knowledge from this network." });
      return;
    }
    if (!allowsDerivatives) {
      addToast({ type: "error", title: "Derivatives restricted", description: "This network is licensed under No-Derivatives (ND) or All Rights Reserved terms." });
      return;
    }
    onFork();
  };

  return (
    <motion.header
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      className="mb-5 rounded-2xl border border-cyan-400/20 bg-[rgba(8,12,22,0.85)] p-5 shadow-2xl backdrop-blur-2xl"
    >
      <div className="flex flex-wrap items-start justify-between gap-4">
        {/* Left: Title, Metadata, Description */}
        <div className="min-w-0 flex-1 space-y-2">
          {/* Concept Entry Context Banner */}
          {fromConcept && (
            <div className="mb-2">
              <Link
                to={`/explore/concepts?q=${encodeURIComponent(fromConcept)}`}
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 hover:bg-cyan-500/20 text-xs font-semibold transition"
              >
                <ArrowLeft size={13} /> Return to Concept Overview: &ldquo;{fromConcept}&rdquo;
              </Link>
            </div>
          )}

          <div className="flex flex-wrap items-center gap-2">
            <span className="flex items-center gap-1.5 rounded-full border border-cyan-400/30 bg-cyan-400/10 px-2.5 py-0.5 text-xs font-semibold text-cyan-200">
              <Globe size={13} className="text-cyan-300" />
              Public Knowledge Network
            </span>

            {isPublished ? (
              <span className="flex items-center gap-1 rounded-full border border-emerald-400/30 bg-emerald-400/10 px-2.5 py-0.5 text-[11px] font-semibold text-emerald-300">
                <CheckCircle2 size={12} />
                PUBLISHED
              </span>
            ) : (
              <span className="flex items-center gap-1 rounded-full border border-amber-400/30 bg-amber-400/10 px-2.5 py-0.5 text-[11px] font-semibold text-amber-300">
                <Lock size={12} />
                PRIVATE DRAFT
              </span>
            )}

            <LicenseBadge licenseType={license} />
          </div>

          <h1 className="text-2xl font-bold tracking-tight text-slate-100 md:text-3xl">
            {graph.title || graph.name}
          </h1>

          {graph.description && (
            <p className="max-w-3xl text-xs leading-relaxed text-slate-300">
              {graph.description}
            </p>
          )}

          <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground pt-1">
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-slate-200">By</span>
              {graph.ownerName ? (
                <Link
                  to={`/creator/${encodeURIComponent(graph.ownerName.toLowerCase().replace(/\s+/g, ""))}`}
                  className="font-medium text-cyan-300 hover:underline flex items-center gap-1"
                >
                  <User size={12} />
                  {graph.ownerName}
                </Link>
              ) : (
                <span className="font-medium text-cyan-200">Community Creator</span>
              )}
            </div>

            <div className="flex items-center gap-1.5">
              <Shield size={13} className="text-cyan-400" />
              <span>{graph.nodeCount ?? 0} Concepts</span>
            </div>

            <div className="flex items-center gap-1.5">
              <GitFork size={13} className="text-violet-400" />
              <span>{graph.edgeCount ?? 0} Relationships</span>
            </div>

            {(graph.derivativeCount ?? 0) > 0 && (
              <div className="flex items-center gap-1.5 text-amber-300">
                <Sparkles size={13} />
                <span>{graph.derivativeCount} Derived Networks</span>
              </div>
            )}
          </div>
        </div>

        {/* Right: Actions */}
        <div className="flex flex-wrap items-center gap-2 shrink-0">
          <Button
            variant="secondary"
            onClick={onOpenRelated}
            className="border-cyan-500/30 bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-300 text-xs font-medium gap-1.5 h-9 px-3"
          >
            <Sparkles size={14} />
            Related Networks
          </Button>

          <Button
            variant="secondary"
            onClick={onOpenProvenance}
            className="border-white/10 bg-white/5 hover:bg-white/10 text-xs font-medium gap-1.5 h-9 px-3"
          >
            <Info size={14} className="text-cyan-300" />
            Provenance & Lineage
          </Button>

          <Button
            variant="secondary"
            onClick={handleShare}
            className="border-white/10 bg-white/5 hover:bg-white/10 text-xs font-medium gap-1.5 h-9 px-3"
          >
            <Share2 size={14} className="text-emerald-300" />
            {copied ? "Copied!" : "Share"}
          </Button>

          {/* Fork / Derive action button */}
          <Button
            variant="secondary"
            disabled={!allowsDerivatives}
            onClick={handleForkClick}
            title={allowsDerivatives ? "Fork and build a derived knowledge network" : "Derivatives restricted by owner license"}
            className={
              allowsDerivatives
                ? "bg-violet-600 text-white font-semibold hover:bg-violet-500 gap-1.5 h-9 px-3"
                : "bg-slate-800 text-slate-400 cursor-not-allowed opacity-60 gap-1.5 h-9 px-3"
            }
          >
            <GitFork size={14} />
            {allowsDerivatives ? "Fork & Derive" : "Derivatives Restricted"}
          </Button>

          {/* Owner Publishing controls */}
          {isOwnerOrEditor && (
            isPublished ? (
              <Button
                variant="secondary"
                onClick={onOpenUnpublishModal}
                className="border-amber-500/30 bg-amber-500/10 text-amber-300 hover:bg-amber-500/20 text-xs font-semibold gap-1.5 h-9 px-3"
              >
                <Lock size={14} />
                Unpublish
              </Button>
            ) : (
              <Button
                variant="primary"
                onClick={onOpenPublishModal}
                className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-1.5 h-9 px-3"
              >
                <Globe size={14} />
                Publish Network
              </Button>
            )
          )}
        </div>
      </div>
    </motion.header>
  );

}
