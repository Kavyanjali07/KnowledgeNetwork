import type { ManagedGraph } from "../types";

type GraphThumbnailProps = {
  graph: ManagedGraph;
};

const nodePositions = [
  "left-[16%] top-[28%]",
  "left-[42%] top-[18%]",
  "left-[68%] top-[34%]",
  "left-[30%] top-[62%]",
  "left-[76%] top-[70%]"
];

export function GraphThumbnail({ graph }: GraphThumbnailProps) {
  return (
    <div className="relative h-full min-h-40 overflow-hidden rounded-xl border border-white/10 bg-[linear-gradient(rgba(255,255,255,0.04)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.04)_1px,transparent_1px)] bg-[length:28px_28px]">
      <div className={`absolute inset-0 bg-gradient-to-br ${graph.accent} opacity-20`} />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_40%,rgba(255,255,255,0.18),transparent_24%)]" />
      <svg className="absolute inset-0 h-full w-full opacity-55">
        <line x1="22%" y1="34%" x2="48%" y2="24%" stroke="rgba(125,211,252,0.52)" strokeWidth="1.6" />
        <line x1="48%" y1="24%" x2="72%" y2="40%" stroke="rgba(167,139,250,0.52)" strokeWidth="1.6" />
        <line x1="36%" y1="68%" x2="72%" y2="40%" stroke="rgba(125,211,252,0.36)" strokeWidth="1.6" />
        <line x1="36%" y1="68%" x2="80%" y2="75%" stroke="rgba(167,139,250,0.38)" strokeWidth="1.6" />
      </svg>
      {nodePositions.map((position, index) => (
        <span
          key={position}
          className={`absolute ${position} h-4 w-4 rounded-full border border-white/40 bg-slate-950 shadow-glow`}
          style={{ opacity: 1 - index * 0.08 }}
        />
      ))}
    </div>
  );
}
