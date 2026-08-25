import { memo, useState } from "react";
import { motion } from "framer-motion";
import { Eye, GitFork, Heart, Lock, MoreHorizontal, Shield, Trash2, Edit3, Link2, Users } from "lucide-react";
import { cn } from "../../../lib/cn";
import type { ManagedGraph } from "../types";
import { GraphThumbnail } from "./GraphThumbnail";
import { useNavigate } from "react-router-dom";

type GraphCardProps = {
  graph: ManagedGraph;
  onEdit?: (graph: ManagedGraph) => void;
  onDelete?: (graph: ManagedGraph) => void;
};

const visibilityIcon = {
  Public: Eye,
  Workspace: Users,
  Private: Lock
};

const heightClass = {
  short: "md:[break-inside:avoid] md:min-h-[420px]",
  medium: "md:[break-inside:avoid] md:min-h-[480px]",
  tall: "md:[break-inside:avoid] md:min-h-[540px]"
};

function formatNumber(value: number) {
  return new Intl.NumberFormat("en", { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

function formatUpdatedDate(value: string) {
  try {
    return new Intl.DateTimeFormat("en", { month: "short", day: "numeric" }).format(new Date(value));
  } catch {
    return "Recently";
  }
}

export const GraphCard = memo(function GraphCard({ graph, onEdit, onDelete }: GraphCardProps) {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const VisibilityIcon = visibilityIcon[graph.visibility] || Lock;

  return (
    <motion.article
      layout
      initial={{ opacity: 0, y: 18, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, scale: 0.98 }}
      whileHover={{ y: -6 }}
      transition={{ duration: 0.24, ease: [0.16, 1, 0.3, 1] }}
      className={cn(
        "group relative mb-4 overflow-hidden rounded-2xl border border-white/10 bg-white/[0.04] p-3 shadow-glass backdrop-blur-xl transition hover:border-cyan-300/30",
        heightClass[graph.height]
      )}
      onClick={() => navigate(`/graphs/${graph.id}`)}
    >
      <div className={graph.height === "tall" ? "h-64" : graph.height === "medium" ? "h-52" : "h-44"}>
        <GraphThumbnail graph={graph} />
      </div>

      <div className="p-2 pt-4">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <h2 className="truncate text-lg font-semibold tracking-[-0.01em]">{graph.title}</h2>
            <p className="mt-2 line-clamp-3 text-sm leading-6 text-muted-foreground">{graph.description}</p>
          </div>
          <button
            type="button"
            aria-label={`Actions for ${graph.title}`}
            onClick={(event) => {
              event.stopPropagation();
              setMenuOpen((value) => !value);
            }}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-muted-foreground transition hover:bg-white/8 hover:text-foreground"
          >
            <MoreHorizontal size={17} />
          </button>
          {menuOpen && (
            <div className="absolute right-4 top-14 z-30 w-44 rounded-xl border border-white/10 bg-[rgba(8,12,20,0.96)] p-1.5 shadow-glass backdrop-blur-2xl">
              <button
                type="button"
                onClick={(event) => {
                  event.stopPropagation();
                  setMenuOpen(false);
                  navigate(`/graphs/${graph.id}`);
                }}
                className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-slate-200 hover:bg-white/8"
              >
                <Eye size={14} className="text-cyan-400" />
                Open graph
              </button>
              {onEdit && (
                <button
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    setMenuOpen(false);
                    onEdit(graph);
                  }}
                  className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-slate-200 hover:bg-white/8"
                >
                  <Edit3 size={14} className="text-amber-400" />
                  Edit metadata
                </button>
              )}
              <button
                type="button"
                onClick={(event) => {
                  event.stopPropagation();
                  navigator.clipboard?.writeText(`${window.location.origin}/graphs/${graph.id}`);
                  setMenuOpen(false);
                }}
                className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-slate-200 hover:bg-white/8"
              >
                <Link2 size={14} className="text-emerald-400" />
                Copy link
              </button>
              {onDelete && (
                <button
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    setMenuOpen(false);
                    onDelete(graph);
                  }}
                  className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-xs font-medium text-rose-300 hover:bg-rose-500/10"
                >
                  <Trash2 size={14} className="text-rose-400" />
                  Delete graph
                </button>
              )}
            </div>
          )}
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          {graph.tags.map((tag) => (
            <span key={tag} className="rounded-full border border-white/10 bg-white/[0.055] px-2.5 py-1 text-xs text-slate-300">
              {tag}
            </span>
          ))}
        </div>

        <div className="mt-5 flex items-center justify-between border-t border-white/10 pt-4">
          <div className="flex items-center gap-2">
            <span className={`flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br ${graph.accent} text-xs font-semibold text-slate-950`}>
              {graph.owner.initials}
            </span>
            <div>
              <p className="text-sm font-medium">{graph.owner.name}</p>
              <p className="text-xs text-muted-foreground">Updated {formatUpdatedDate(graph.updatedAt)}</p>
            </div>
          </div>
          <span className="flex items-center gap-1.5 rounded-full border border-white/10 bg-black/18 px-2.5 py-1 text-xs text-muted-foreground">
            <VisibilityIcon size={13} />
            {graph.visibility}
          </span>
        </div>

        <div className="mt-4 grid grid-cols-4 gap-2 text-xs text-muted-foreground">
          <div className="rounded-lg border border-white/10 bg-black/16 p-2">
            <Heart size={14} className="mb-1 text-rose-200" />
            {formatNumber(graph.likes)}
          </div>
          <div className="rounded-lg border border-white/10 bg-black/16 p-2">
            <GitFork size={14} className="mb-1 text-cyan-200" />
            {formatNumber(graph.forks)}
          </div>
          <div className="rounded-lg border border-white/10 bg-black/16 p-2">
            <Shield size={14} className="mb-1 text-emerald-200" />
            {formatNumber(graph.nodes)}
          </div>
          <div className="rounded-lg border border-white/10 bg-black/16 p-2">
            <Users size={14} className="mb-1 text-violet-200" />
            {formatNumber(graph.edges)}
          </div>
        </div>
      </div>
    </motion.article>
  );
});
