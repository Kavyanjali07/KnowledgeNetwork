import React from "react";
import { Link } from "react-router-dom";
import { Compass, Network, User, Link2, ChevronRight, History } from "lucide-react";
import { useKnowledgeTrail, KnowledgeTrailStep } from "../../features/explore/hooks/useKnowledgeTrail";

export function KnowledgeTrailBar() {
  const { trail, clearTrail } = useKnowledgeTrail();

  if (trail.length <= 1) {
    return null;
  }

  const getStepIcon = (type: KnowledgeTrailStep["type"]) => {
    switch (type) {
      case "hub":
        return <Compass size={13} className="text-cyan-400" />;
      case "concept":
        return <Link2 size={13} className="text-violet-400" />;
      case "network":
        return <Network size={13} className="text-emerald-400" />;
      case "creator":
        return <User size={13} className="text-amber-400" />;
    }
  };

  return (
    <div className="w-full bg-[#080c14]/90 border-b border-white/10 px-4 py-2 flex items-center justify-between text-xs backdrop-blur-md select-none">
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
        <span className="flex items-center gap-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider shrink-0">
          <History size={12} className="text-cyan-400" /> Knowledge Trail:
        </span>

        {trail.map((step, idx) => {
          const isLast = idx === trail.length - 1;

          return (
            <React.Fragment key={step.id}>
              {idx > 0 && <ChevronRight size={12} className="text-slate-600 shrink-0" />}

              {isLast ? (
                <span className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-200 font-medium shrink-0">
                  {getStepIcon(step.type)}
                  <span className="truncate max-w-[150px]">{step.label}</span>
                </span>
              ) : (
                <Link
                  to={step.url}
                  className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-white/5 border border-white/10 text-slate-300 hover:text-cyan-300 hover:border-cyan-500/30 transition shrink-0"
                >
                  {getStepIcon(step.type)}
                  <span className="truncate max-w-[130px]">{step.label}</span>
                </Link>
              )}
            </React.Fragment>
          );
        })}
      </div>

      <button
        onClick={clearTrail}
        className="text-[10px] text-slate-500 hover:text-slate-300 transition shrink-0 ml-2"
        title="Clear Exploration Trail"
      >
        Clear
      </button>
    </div>
  );
}
