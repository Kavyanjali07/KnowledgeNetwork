import type { NodeProps } from "@xyflow/react";
import { Handle, Position } from "@xyflow/react";
import { FileText, Lightbulb, Scale, UserRound } from "lucide-react";
import { cn } from "../../../lib/cn";
import type { KnowledgeNodeData } from "../types";

const typeStyles = {
  Concept: {
    icon: Lightbulb,
    className: "from-cyan-300/22 to-blue-500/10 text-cyan-100"
  },
  Document: {
    icon: FileText,
    className: "from-violet-300/22 to-fuchsia-500/10 text-violet-100"
  },
  Person: {
    icon: UserRound,
    className: "from-emerald-300/22 to-cyan-500/10 text-emerald-100"
  },
  Decision: {
    icon: Scale,
    className: "from-amber-300/22 to-violet-500/10 text-amber-100"
  }
};

export function KnowledgeNode({ data, selected }: NodeProps) {
  const nodeData = data as KnowledgeNodeData;
  const style = typeStyles[nodeData.type];
  const Icon = style.icon;

  return (
    <div
      className={cn(
        "w-[260px] rounded-xl border bg-[rgba(8,12,20,0.86)] p-3 shadow-glass backdrop-blur-xl transition duration-200",
        selected ? "border-cyan-300/55 shadow-glow" : "border-white/12 hover:border-cyan-300/30"
      )}
    >
      <Handle
        type="target"
        position={Position.Left}
        className="!h-3 !w-3 !border !border-cyan-200/50 !bg-slate-950"
      />
      <Handle
        type="source"
        position={Position.Right}
        className="!h-3 !w-3 !border !border-cyan-200/50 !bg-slate-950"
      />

      <div className={cn("rounded-lg border border-white/10 bg-gradient-to-br p-3", style.className)}>
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg border border-white/10 bg-black/20">
            <Icon size={16} />
          </span>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-white">{nodeData.title}</p>
            <p className="text-xs text-slate-300">{nodeData.type}</p>
          </div>
        </div>
      </div>

      <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-300">{nodeData.description}</p>

      <div className="mt-3 flex items-center justify-between border-t border-white/10 pt-3 text-xs text-muted-foreground">
        <span>{nodeData.connections} links</span>
        <span>{nodeData.confidence}% confidence</span>
      </div>
    </div>
  );
}
