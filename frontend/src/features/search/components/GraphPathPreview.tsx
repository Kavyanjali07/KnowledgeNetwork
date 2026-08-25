import { ArrowRight, GitFork, Layers, Network } from "lucide-react";

interface GraphPathPreviewProps {
  sourceNode?: string;
  targetNode?: string;
  relationshipType?: string;
  confidence?: number;
}

export function GraphPathPreview({
  sourceNode,
  targetNode,
  relationshipType = "connected",
  confidence
}: GraphPathPreviewProps) {
  if (!sourceNode || !targetNode) return null;

  return (
    <div className="mt-2 flex flex-wrap items-center gap-2 rounded-lg border border-cyan-500/20 bg-cyan-950/20 px-3 py-2 text-xs text-slate-300">
      <div className="flex items-center gap-1.5 font-medium text-cyan-200">
        <Layers size={13} className="text-cyan-400" />
        <span className="truncate max-w-[140px] sm:max-w-[180px]">{sourceNode}</span>
      </div>

      <div className="flex items-center gap-1 rounded-full border border-violet-500/30 bg-violet-900/40 px-2 py-0.5 text-[11px] font-mono text-violet-300">
        <GitFork size={10} className="rotate-90 text-violet-400" />
        <span className="uppercase tracking-wider">{relationshipType}</span>
        <ArrowRight size={12} className="text-cyan-400" />
      </div>

      <div className="flex items-center gap-1.5 font-medium text-cyan-200">
        <Network size={13} className="text-cyan-400" />
        <span className="truncate max-w-[140px] sm:max-w-[180px]">{targetNode}</span>
      </div>

      {confidence && (
        <span className="ml-auto rounded bg-emerald-950/80 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-400 border border-emerald-500/30">
          {confidence}% match
        </span>
      )}
    </div>
  );
}
