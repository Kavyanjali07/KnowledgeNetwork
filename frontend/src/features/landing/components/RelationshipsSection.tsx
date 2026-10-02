import { motion, useInView } from "framer-motion";
import { useRef, useState } from "react";

const relationships = [
  { type: "Prerequisite", color: "#22d3ee", desc: "Must come before", example: "Linear Algebra → Machine Learning" },
  { type: "Depends on", color: "#a78bfa", desc: "Relies on to function", example: "Training → Data Pipeline" },
  { type: "Part of", color: "#34d399", desc: "Is a component of", example: "Neural Networks → Deep Learning" },
  { type: "Causes", color: "#fbbf24", desc: "Leads to or produces", example: "Bias in Data → Unfair Models" },
  { type: "Supports", color: "#60a5fa", desc: "Provides evidence for", example: "Research Paper → Hypothesis" },
  { type: "Contradicts", color: "#f87171", desc: "Conflicts with", example: "Study A → Study B findings" },
  { type: "References", color: "#c084fc", desc: "Cites or mentions", example: "Blog Post → Original Paper" },
  { type: "Related to", color: "#94a3b8", desc: "General association", example: "UX Design → Psychology" },
];

export function RelationshipsSection() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-80px" });
  const [active, setActive] = useState(0);

  return (
    <section className="relative py-24 md:py-36" ref={ref}>
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />

      <div className="mx-auto max-w-[1200px] px-6 md:px-10">
        <motion.div
          className="mx-auto max-w-3xl text-center"
          initial={{ opacity: 0, y: 24 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.7 }}
        >
          <p className="text-sm font-medium uppercase tracking-widest text-cyan-400">Meaningful connections</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            Not just links.
            <br />
            <span className="text-slate-500">Relationships with meaning.</span>
          </h2>
          <p className="mt-5 text-base leading-relaxed text-slate-400 md:text-lg">
            Every connection between concepts carries semantic meaning.
            Your graph doesn't just show what's connected — it shows <em>why</em>.
          </p>
        </motion.div>

        {/* Relationship type cards */}
        <div className="mx-auto mt-14 grid max-w-4xl gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {relationships.map((rel, i) => (
            <motion.button
              key={rel.type}
              className={`group rounded-xl border p-4 text-left transition ${
                active === i
                  ? "border-white/20 bg-white/[0.06]"
                  : "border-white/8 bg-white/[0.02] hover:border-white/14 hover:bg-white/[0.04]"
              }`}
              onClick={() => setActive(i)}
              initial={{ opacity: 0, y: 16 }}
              animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ delay: 0.3 + i * 0.06, duration: 0.5 }}
            >
              <div className="flex items-center gap-2">
                <span
                  className="h-2.5 w-2.5 rounded-full"
                  style={{ backgroundColor: rel.color }}
                />
                <span className="text-sm font-semibold text-white">{rel.type}</span>
              </div>
              <p className="mt-2 text-xs text-slate-500">{rel.desc}</p>
            </motion.button>
          ))}
        </div>

        {/* Active relationship visual */}
        <motion.div
          className="mx-auto mt-8 max-w-2xl"
          key={active}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
        >
          <div className="flex items-center justify-center gap-4 rounded-2xl border border-white/10 bg-white/[0.03] px-8 py-6">
            <div className="rounded-xl border border-white/12 bg-white/[0.04] px-5 py-3">
              <p className="text-sm font-medium text-white">
                {relationships[active]?.example?.split("→")[0]?.trim()}
              </p>
            </div>
            <div className="flex flex-col items-center gap-1">
              <div
                className="h-0.5 w-16 rounded-full sm:w-24"
                style={{ backgroundColor: relationships[active]?.color ?? "#94a3b8" }}
              />
              <span
                className="text-xs font-medium"
                style={{ color: relationships[active]?.color ?? "#94a3b8" }}
              >
                {relationships[active]?.type}
              </span>
            </div>
            <div className="rounded-xl border border-white/12 bg-white/[0.04] px-5 py-3">
              <p className="text-sm font-medium text-white">
                {relationships[active]?.example?.split("→")[1]?.trim()}
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
