import { motion } from "framer-motion";
import { GitFork } from "lucide-react";
import { useForkGraph } from "../../../hooks/use-mutations";
import type { VersionEntryResponse } from "../../../services";

type ForkGraphProps = {
  versionHistory: VersionEntryResponse[];
  onSelectCommit: (id: string) => void;
  selectedCommitId: string | null;
  workspaceId: string;
};

const branchColor: Record<string, string> = {
  main: "#22d3ee",
  "ontology-cleanup": "#34d399",
  "pricing-model": "#a78bfa",
  "vector-search": "#fbbf24",
  "ai-suggestions": "#e879f9"
};

export function ForkGraph({ versionHistory, onSelectCommit, selectedCommitId, workspaceId }: ForkGraphProps) {
  const forkMutation = useForkGraph(workspaceId);

  const nodes = versionHistory.map((commit, index) => {
    const angle = (index / versionHistory.length) * Math.PI * 2;
    const radius = 30 + (index % 3) * 10;
    return {
      id: `fn-${commit.id}`,
      commitId: commit.id,
      branch: commit.label ?? "main",
      x: 50 + Math.cos(angle) * radius,
      y: 50 + Math.sin(angle) * radius
    };
  });

  const edges = versionHistory.slice(1).map((_commit, index) => {
    const fromNode = nodes[index];
    const toNode = nodes[index + 1];
    if (!fromNode || !toNode) return null;
    return { from: fromNode.id, to: toNode.id, type: index === 2 || index === 5 ? "merge" as const : "parent" as const };
  }).filter((e): e is NonNullable<typeof e> => e !== null);

  const nodeMap = new Map(nodes.map((n) => [n.id, n]));

  return (
    <div className="relative w-full overflow-hidden rounded-2xl border border-white/10 bg-white/[0.03] p-4">
      <div className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-slate-200">Fork graph</p>
          <p className="text-xs text-muted-foreground">Branch topology and merge points</p>
        </div>
      </div>

      <svg viewBox="0 0 100 100" className="h-64 w-full" preserveAspectRatio="none">
        <defs>
          <linearGradient id="edgeGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="rgba(255,255,255,0.35)" />
            <stop offset="100%" stopColor="rgba(255,255,255,0.08)" />
          </linearGradient>
        </defs>

        {edges.map((edge, index) => {
          const from = nodeMap.get(edge.from);
          const to = nodeMap.get(edge.to);
          if (!from || !to) return null;

          const color = edge.type === "merge" ? "rgba(167,139,250,0.55)" : "rgba(125,211,252,0.45)";
          const width = edge.type === "merge" ? "0.55" : "0.35";

          return (
            <motion.line
              key={index}
              x1={`${from.x}%`}
              y1={`${from.y}%`}
              x2={`${to.x}%`}
              y2={`${to.y}%`}
              stroke={color}
              strokeWidth={width}
              initial={{ pathLength: 0 }}
              whileInView={{ pathLength: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 1.1, ease: "easeInOut", delay: index * 0.04 }}
            />
          );
        })}

        {nodes.map((node, index) => {
          const commit = versionHistory.find((c) => c.id === node.commitId);
          const isSelected = selectedCommitId === node.commitId;
          const color = branchColor[node.branch] ?? "#94a3b8";

          return (
            <motion.g
              key={node.id}
              initial={{ opacity: 0, scale: 0.6 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.05, type: "spring", stiffness: 260, damping: 18 }}
              onClick={() => commit && onSelectCommit(commit.id)}
              className="cursor-pointer"
            >
              <circle cx={`${node.x}%`} cy={`${node.y}%`} r="2.8" fill={color} opacity={isSelected ? "1" : "0.7"} />
              {isSelected && (
                <circle cx={`${node.x}%`} cy={`${node.y}%`} r="4.4" fill="none" stroke={color} strokeWidth="0.6" opacity="0.6" />
              )}
            </motion.g>
          );
        })}
      </svg>

      <div className="mt-3 flex flex-wrap gap-2">
        {Object.entries(branchColor).map(([branch, color]) => (
          <span key={branch} className="flex items-center gap-1.5 text-[10px] text-slate-400">
            <span className="h-2 w-2 rounded-full" style={{ backgroundColor: color }} />
            {branch}
          </span>
        ))}
      </div>

      {selectedCommitId && (
        <div className="mt-3 flex justify-end">
          <button
            onClick={() => {
              const commit = versionHistory.find((c) => c.id === selectedCommitId);
              if (commit) {
                forkMutation.mutate(
                  { sourceVersionId: commit.id, name: `Fork from ${commit.label}`, description: `Forked from version ${commit.versionNumber}` },
                  {
                    onSuccess: () => {
                      forkMutation.reset();
                    }
                  }
                );
              }
            }}
            className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-1.5 text-[11px] text-slate-300 transition hover:border-violet-300/40 hover:text-violet-200"
          >
            <GitFork size={12} />
            Fork this version
          </button>
        </div>
      )}
    </div>
  );
}
