import { motion, useInView } from "framer-motion";
import { useRef, useState } from "react";
import { ArrowRight, Compass, GitBranch, Layers } from "lucide-react";

const queries = [
  {
    question: "What connects to this?",
    desc: "See every concept linked to a node and understand the full context.",
    icon: GitBranch,
    visual: ["Machine Learning", "→ Statistics", "→ Python", "→ Data Science", "→ Visualization"],
  },
  {
    question: "What leads here?",
    desc: "Trace prerequisites and dependencies backwards through your graph.",
    icon: Layers,
    visual: ["Linear Algebra", "→ Calculus", "→ Optimization", "→ Neural Networks"],
  },
  {
    question: "What depends on this?",
    desc: "Discover everything that relies on a concept before you modify it.",
    icon: Compass,
    visual: ["API Design", "→ Frontend App", "→ Mobile Client", "→ Documentation"],
  },
];

export function ExploreSection() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-80px" });
  const [activeQuery, setActiveQuery] = useState(0);

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
          <p className="text-sm font-medium uppercase tracking-widest text-violet-400">Navigate knowledge</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            Ask your graph
            <br />
            <span className="text-slate-500">a question.</span>
          </h2>
        </motion.div>

        <div className="mx-auto mt-14 grid max-w-4xl gap-6 md:grid-cols-[1fr_1.2fr]">
          {/* Query selector */}
          <div className="space-y-3">
            {queries.map((q, i) => {
              const Icon = q.icon;
              return (
                <motion.button
                  key={q.question}
                  className={`group w-full rounded-xl border p-5 text-left transition ${
                    activeQuery === i
                      ? "border-violet-400/30 bg-violet-500/[0.06]"
                      : "border-white/8 bg-white/[0.02] hover:border-white/14 hover:bg-white/[0.04]"
                  }`}
                  onClick={() => setActiveQuery(i)}
                  initial={{ opacity: 0, x: -16 }}
                  animate={inView ? { opacity: 1, x: 0 } : {}}
                  transition={{ delay: 0.3 + i * 0.1, duration: 0.5 }}
                >
                  <div className="flex items-center gap-3">
                    <Icon size={18} className={activeQuery === i ? "text-violet-400" : "text-slate-500"} />
                    <span className="text-base font-semibold text-white">{q.question}</span>
                  </div>
                  <p className="mt-2 text-sm text-slate-500">{q.desc}</p>
                </motion.button>
              );
            })}
          </div>

          {/* Visual result */}
          <motion.div
            className="flex items-center justify-center rounded-2xl border border-white/10 bg-white/[0.02] p-8"
            key={activeQuery}
            initial={{ opacity: 0, scale: 0.97 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.35 }}
          >
            <div className="space-y-3">
              {(queries[activeQuery]?.visual ?? []).map((step, i) => (
                <motion.div
                  key={step}
                  className="flex items-center gap-3"
                  initial={{ opacity: 0, x: 12 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: i * 0.1, duration: 0.4 }}
                >
                  {i > 0 && <ArrowRight size={14} className="text-cyan-400/60" />}
                  <span
                    className={`rounded-lg border px-4 py-2 text-sm font-medium ${
                      i === 0
                        ? "border-cyan-400/30 bg-cyan-400/10 text-cyan-200"
                        : "border-white/10 bg-white/[0.04] text-slate-300"
                    }`}
                  >
                    {step.replace("→ ", "")}
                  </span>
                </motion.div>
              ))}
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
