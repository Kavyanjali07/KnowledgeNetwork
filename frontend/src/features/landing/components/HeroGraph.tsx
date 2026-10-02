import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Sparkles, X, Network } from "lucide-react";

// Example graph data: "Understanding AI"
const NODES = [
  { id: "ai", label: "Artificial Intelligence", type: "Concept", x: 500, y: 120, size: 52, description: "The overarching domain of building intelligent systems capable of learning and reasoning." },
  { id: "ml", label: "Machine Learning", type: "Concept", x: 280, y: 270, size: 46, description: "Algorithms that learn patterns from data to make predictions without explicit programming." },
  { id: "nn", label: "Neural Networks", type: "Technology", x: 720, y: 270, size: 44, description: "Computing systems inspired by biological neural networks to recognize complex patterns." },
  { id: "nlp", label: "Natural Language Processing", type: "Concept", x: 160, y: 430, size: 40, description: "Techniques enabling machines to analyze, understand, and generate human language." },
  { id: "cv", label: "Computer Vision", type: "Concept", x: 400, y: 430, size: 40, description: "Systems designed to extract meaningful information from visual inputs like digital images and videos." },
  { id: "transformer", label: "Transformer Architecture", type: "Technology", x: 640, y: 430, size: 42, description: "Deep learning model utilizing self-attention mechanisms to process sequential data." },
  { id: "data", label: "Training Data", type: "Resource", x: 860, y: 430, size: 38, description: "Curated datasets used to train models and establish statistical feature weights." },
  { id: "ethics", label: "AI Ethics", type: "Concept", x: 300, y: 580, size: 38, description: "Principles and guidelines ensuring AI technology remains fair, safe, and beneficial." },
  { id: "bias", label: "Model Bias", type: "Decision", x: 540, y: 580, size: 36, description: "Systematic errors or unfair outputs caused by skewed dataset distributions or model parameters." },
  { id: "gpt", label: "Large Language Models", type: "Technology", x: 760, y: 580, size: 40, description: "Foundation models trained on vast text corpora to perform diverse language understanding tasks." },
];

const EDGES = [
  { from: "ai", to: "ml", label: "includes" },
  { from: "ai", to: "nn", label: "includes" },
  { from: "ml", to: "nlp", label: "enables" },
  { from: "ml", to: "cv", label: "enables" },
  { from: "nn", to: "transformer", label: "evolved into" },
  { from: "nn", to: "data", label: "requires" },
  { from: "transformer", to: "gpt", label: "powers" },
  { from: "data", to: "bias", label: "can cause" },
  { from: "ethics", to: "bias", label: "addresses" },
  { from: "nlp", to: "transformer", label: "uses" },
  { from: "ethics", to: "gpt", label: "governs" },
];

const TYPE_COLORS: Record<string, { bg: string; border: string; glow: string }> = {
  Concept: { bg: "rgba(34,211,238,0.12)", border: "rgba(34,211,238,0.45)", glow: "rgba(34,211,238,0.2)" },
  Technology: { bg: "rgba(167,139,250,0.12)", border: "rgba(167,139,250,0.45)", glow: "rgba(167,139,250,0.2)" },
  Resource: { bg: "rgba(52,211,153,0.12)", border: "rgba(52,211,153,0.45)", glow: "rgba(52,211,153,0.2)" },
  Decision: { bg: "rgba(251,191,36,0.12)", border: "rgba(251,191,36,0.45)", glow: "rgba(251,191,36,0.2)" },
};

const TYPE_TEXT_COLORS: Record<string, string> = {
  Concept: "rgba(34,211,238,0.95)",
  Technology: "rgba(167,139,250,0.95)",
  Resource: "rgba(52,211,153,0.95)",
  Decision: "rgba(251,191,36,0.95)",
};

function getNodeCenter(node: typeof NODES[0]) {
  return { cx: node.x, cy: node.y };
}

export function HeroGraph() {
  const svgRef = useRef<SVGSVGElement>(null);
  const [activeNodeId, setActiveNodeId] = useState<string | null>("ml");
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);

  const activeOrHoveredId = hoveredNodeId || activeNodeId;

  const connectedNodeIds = useMemo(() => {
    if (!activeOrHoveredId) return new Set<string>();
    const set = new Set<string>([activeOrHoveredId]);
    EDGES.forEach((e) => {
      if (e.from === activeOrHoveredId) set.add(e.to);
      if (e.to === activeOrHoveredId) set.add(e.from);
    });
    return set;
  }, [activeOrHoveredId]);

  const activeNode = useMemo(() => {
    return NODES.find((n) => n.id === activeOrHoveredId);
  }, [activeOrHoveredId]);

  const connectedRelationships = useMemo(() => {
    if (!activeOrHoveredId) return [];
    return EDGES.filter((e) => e.from === activeOrHoveredId || e.to === activeOrHoveredId).map((e) => {
      const isOutgoing = e.from === activeOrHoveredId;
      const targetId = isOutgoing ? e.to : e.from;
      const targetNode = NODES.find((n) => n.id === targetId);
      return {
        label: e.label,
        direction: isOutgoing ? "outgoing" : "incoming",
        targetLabel: targetNode?.label || targetId,
      };
    });
  }, [activeOrHoveredId]);

  const prefersReducedMotion = useMemo(() => {
    if (typeof window === "undefined") return false;
    return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }, []);

  const animateNodes = useCallback(() => {
    if (prefersReducedMotion || !svgRef.current) return;
    const groups = svgRef.current.querySelectorAll<SVGGElement>("[data-node-group]");
    groups.forEach((g, i) => {
      const offset = i * 0.7;
      const animate = () => {
        const t = (Date.now() / 1000 + offset) % (Math.PI * 2);
        const dy = Math.sin(t * 0.5) * 3;
        const dx = Math.cos(t * 0.3) * 2;
        g.style.transform = `translate(${dx}px, ${dy}px)`;
        requestAnimationFrame(animate);
      };
      requestAnimationFrame(animate);
    });
  }, [prefersReducedMotion]);

  useEffect(() => {
    animateNodes();
  }, [animateNodes]);

  return (
    <motion.div
      className="relative mx-auto w-full max-w-[960px] rounded-2xl border border-white/10 bg-slate-950/60 p-4 backdrop-blur-xl md:p-6"
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 1.2, ease: [0.16, 1, 0.3, 1], delay: 0.3 }}
    >
      {/* Interactive Helper Banner */}
      <div className="mb-2 flex items-center justify-between border-b border-white/5 pb-3">
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <Sparkles size={14} className="text-cyan-400" />
          <span>Click or hover any concept to explore connected relationships</span>
        </div>
        <div className="hidden text-xs font-mono text-cyan-400/80 sm:block">
          Interactive Demonstration • 10 Concepts • 11 Semantic Edges
        </div>
      </div>

      {/* Atmospheric glow behind graph */}
      <div className="pointer-events-none absolute inset-0 -inset-x-20">
        <div className="absolute left-1/4 top-1/4 h-64 w-64 rounded-full bg-cyan-500/8 blur-[100px]" />
        <div className="absolute right-1/4 top-1/3 h-48 w-48 rounded-full bg-violet-500/8 blur-[80px]" />
        <div className="absolute bottom-1/4 left-1/3 h-40 w-40 rounded-full bg-emerald-500/6 blur-[70px]" />
      </div>

      <svg
        ref={svgRef}
        viewBox="0 0 1000 700"
        className="relative h-auto w-full"
        role="img"
        aria-label="Interactive knowledge graph visualization showing AI concepts and their relationships"
      >
        <defs>
          <filter id="hero-glow">
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feMerge>
              <feMergeNode in="blur" />
              <feMergeNode in="SourceGraphic" />
            </feMerge>
          </filter>
          <linearGradient id="edge-gradient" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="rgba(34,211,238,0.6)" />
            <stop offset="100%" stopColor="rgba(167,139,250,0.6)" />
          </linearGradient>
          <linearGradient id="edge-active" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="rgba(34,211,238,0.95)" />
            <stop offset="100%" stopColor="rgba(167,139,250,0.95)" />
          </linearGradient>
        </defs>

        {/* Edges */}
        {EDGES.map((edge, i) => {
          const fromNode = NODES.find((n) => n.id === edge.from);
          const toNode = NODES.find((n) => n.id === edge.to);
          if (!fromNode || !toNode) return null;
          const from = getNodeCenter(fromNode);
          const to = getNodeCenter(toNode);
          const midX = (from.cx + to.cx) / 2;
          const midY = (from.cy + to.cy) / 2 - 20;

          const isConnectedToActive =
            activeOrHoveredId && (edge.from === activeOrHoveredId || edge.to === activeOrHoveredId);
          const isDimmed = activeOrHoveredId && !isConnectedToActive;

          return (
            <g key={`edge-${i}`} className="transition-opacity duration-300" style={{ opacity: isDimmed ? 0.15 : 1 }}>
              <motion.path
                d={`M${from.cx},${from.cy} Q${midX},${midY} ${to.cx},${to.cy}`}
                fill="none"
                stroke={isConnectedToActive ? "url(#edge-active)" : "url(#edge-gradient)"}
                strokeWidth={isConnectedToActive ? "2.5" : "1.5"}
                strokeLinecap="round"
                initial={{ pathLength: 0, opacity: 0 }}
                animate={{ pathLength: 1, opacity: isDimmed ? 0.15 : 1 }}
                transition={{ duration: 1.5, delay: 0.8 + i * 0.08, ease: "easeOut" }}
              />
              {/* Edge label */}
              <motion.text
                x={midX}
                y={midY + 4}
                textAnchor="middle"
                fill={isConnectedToActive ? "rgba(226,232,240,0.95)" : "rgba(148,163,184,0.6)"}
                fontSize={isConnectedToActive ? "11" : "10"}
                fontWeight={isConnectedToActive ? "600" : "400"}
                fontFamily="Inter, system-ui, sans-serif"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 2 + i * 0.06 }}
              >
                {edge.label}
              </motion.text>
            </g>
          );
        })}

        {/* Nodes */}
        {NODES.map((node, i) => {
          const colors = TYPE_COLORS[node.type] || TYPE_COLORS.Concept;
          const textColor = TYPE_TEXT_COLORS[node.type] || TYPE_TEXT_COLORS.Concept;
          const isSelected = activeNodeId === node.id;
          const isHovered = hoveredNodeId === node.id;
          const isConnected = connectedNodeIds.has(node.id);
          const isDimmed = activeOrHoveredId && !isConnected;

          return (
            <motion.g
              key={node.id}
              data-node-group
              initial={{ opacity: 0, scale: 0 }}
              animate={{ opacity: isDimmed ? 0.3 : 1, scale: isSelected || isHovered ? 1.08 : 1 }}
              transition={{
                duration: 0.4,
                delay: 0.5 + i * 0.08,
                ease: [0.16, 1, 0.3, 1],
              }}
              style={{ transformOrigin: `${node.x}px ${node.y}px`, cursor: "pointer" }}
              onClick={() => setActiveNodeId(isSelected ? null : node.id)}
              onMouseEnter={() => setHoveredNodeId(node.id)}
              onMouseLeave={() => setHoveredNodeId(null)}
            >
              {/* Glow circle */}
              <circle
                cx={node.x}
                cy={node.y}
                r={node.size + (isSelected || isHovered ? 12 : 8)}
                fill={colors.glow}
                filter="url(#hero-glow)"
              />
              {/* Main circle */}
              <circle
                cx={node.x}
                cy={node.y}
                r={node.size}
                fill={colors.bg}
                stroke={isSelected ? "#22D3EE" : isHovered ? "rgba(255,255,255,0.7)" : colors.border}
                strokeWidth={isSelected ? "3" : isHovered ? "2.5" : "1.5"}
                className="transition-all duration-300"
              />
              {/* Node label */}
              <text
                x={node.x}
                y={node.y - 6}
                textAnchor="middle"
                fill={isSelected ? "#FFFFFF" : "rgba(226,232,240,0.95)"}
                fontSize="12"
                fontWeight={isSelected ? "700" : "600"}
                fontFamily="Inter, system-ui, sans-serif"
              >
                {node.label}
              </text>
              {/* Type label */}
              <text
                x={node.x}
                y={node.y + 10}
                textAnchor="middle"
                fill={textColor}
                fontSize="9"
                fontFamily="Inter, system-ui, sans-serif"
              >
                {node.type}
              </text>
            </motion.g>
          );
        })}
      </svg>

      {/* Interactive Concept Inspector Overlay Panel */}
      <AnimatePresence>
        {activeNode && (
          <motion.div
            className="absolute bottom-4 left-4 right-4 z-20 rounded-xl border border-white/12 bg-slate-900/90 p-4 shadow-2xl backdrop-blur-xl sm:left-6 sm:right-auto sm:max-w-md"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 10 }}
            transition={{ duration: 0.3 }}
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="flex items-center gap-2">
                  <span
                    className="inline-block h-2.5 w-2.5 rounded-full"
                    style={{ backgroundColor: TYPE_TEXT_COLORS[activeNode.type] || "#22D3EE" }}
                  />
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                    {activeNode.type} Concept
                  </span>
                </div>
                <h4 className="mt-1 text-base font-bold text-white sm:text-lg">{activeNode.label}</h4>
              </div>
              <button
                onClick={() => setActiveNodeId(null)}
                className="rounded-lg p-1 text-slate-400 transition hover:bg-white/10 hover:text-white"
                aria-label="Close inspector"
              >
                <X size={16} />
              </button>
            </div>

            <p className="mt-2 text-xs leading-relaxed text-slate-300 sm:text-sm">{activeNode.description}</p>

            {connectedRelationships.length > 0 && (
              <div className="mt-3 border-t border-white/10 pt-3">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-cyan-400">
                  <Network size={12} />
                  <span>Connected Relationships ({connectedRelationships.length}):</span>
                </div>
                <div className="mt-2 flex flex-wrap gap-1.5">
                  {connectedRelationships.map((rel, idx) => (
                    <span
                      key={idx}
                      className="inline-flex items-center gap-1 rounded-md border border-cyan-500/20 bg-cyan-500/10 px-2 py-1 text-xs text-slate-200"
                    >
                      <span className="font-semibold text-cyan-300">{rel.label}</span>
                      <span className="text-slate-400">→</span>
                      <span>{rel.targetLabel}</span>
                    </span>
                  ))}
                </div>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

