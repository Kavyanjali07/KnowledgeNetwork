import { motion, useInView } from "framer-motion";
import { useRef } from "react";

// Mini graph that "forms" as user scrolls into view
const miniNodes = [
  { id: "a", label: "Machine Learning", x: 200, y: 80 },
  { id: "b", label: "Data Science", x: 400, y: 60 },
  { id: "c", label: "Statistics", x: 100, y: 220 },
  { id: "d", label: "Python", x: 320, y: 220 },
  { id: "e", label: "Visualization", x: 500, y: 200 },
  { id: "f", label: "Deep Learning", x: 250, y: 340 },
];

const miniEdges = [
  { from: 0, to: 1 },
  { from: 0, to: 2 },
  { from: 0, to: 3 },
  { from: 1, to: 4 },
  { from: 1, to: 3 },
  { from: 0, to: 5 },
  { from: 3, to: 5 },
];

export function TransformationSection() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-100px" });

  return (
    <section className="relative py-24 md:py-36" ref={ref}>
      {/* Subtle divider gradient */}
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />

      <div className="mx-auto max-w-[1200px] px-6 md:px-10">
        <motion.div
          className="mx-auto max-w-3xl text-center"
          initial={{ opacity: 0, y: 24 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        >
          <p className="text-sm font-medium uppercase tracking-widest text-violet-400">The shift</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            Knowledge becomes useful when
            <br />
            <span className="bg-gradient-to-r from-violet-400 to-cyan-400 bg-clip-text text-transparent">
              connections become visible.
            </span>
          </h2>
          <p className="mt-5 text-base leading-relaxed text-slate-400 md:text-lg">
            KnowledgeNetwork turns isolated information into a living graph.
            Every concept you add connects to what you already know.
          </p>
        </motion.div>

        {/* Animated mini-graph formation */}
        <div className="relative mx-auto mt-16 max-w-2xl">
          <svg viewBox="0 0 600 400" className="h-auto w-full" aria-hidden="true">
            {/* Edges draw on */}
            {miniEdges.map((edge, i) => {
              const from = miniNodes[edge.from];
              const to = miniNodes[edge.to];
              if (!from || !to) return null;
              return (
                <motion.line
                  key={`te-${i}`}
                  x1={from.x}
                  y1={from.y}
                  x2={to.x}
                  y2={to.y}
                  stroke="rgba(34,211,238,0.25)"
                  strokeWidth="1.5"
                  strokeLinecap="round"
                  initial={{ pathLength: 0, opacity: 0 }}
                  animate={inView ? { pathLength: 1, opacity: 1 } : {}}
                  transition={{ duration: 1, delay: 0.5 + i * 0.12, ease: "easeOut" }}
                />
              );
            })}

            {/* Nodes appear */}
            {miniNodes.map((node, i) => (
              <motion.g
                key={node.id}
                initial={{ opacity: 0, scale: 0 }}
                animate={inView ? { opacity: 1, scale: 1 } : {}}
                transition={{ delay: 0.3 + i * 0.1, duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
                style={{ transformOrigin: `${node.x}px ${node.y}px` }}
              >
                <circle cx={node.x} cy={node.y} r="32" fill="rgba(34,211,238,0.08)" stroke="rgba(34,211,238,0.25)" strokeWidth="1" />
                <text x={node.x} y={node.y + 1} textAnchor="middle" fill="rgba(226,232,240,0.85)" fontSize="10" fontWeight="500" fontFamily="Inter, system-ui, sans-serif" dominantBaseline="middle">
                  {node.label}
                </text>
              </motion.g>
            ))}
          </svg>
        </div>
      </div>
    </section>
  );
}
