import { motion, useInView } from "framer-motion";
import { useRef } from "react";
import { Clock, GitBranch, Plus } from "lucide-react";

const timelineSteps = [
  { label: "Week 1", nodes: ["Java Basics", "OOP Concepts"], edges: 1 },
  { label: "Week 3", nodes: ["Java Basics", "OOP Concepts", "Design Patterns", "Spring Boot"], edges: 4 },
  { label: "Month 2", nodes: ["Java Basics", "OOP Concepts", "Design Patterns", "Spring Boot", "REST APIs", "Testing", "Microservices"], edges: 9 },
];

export function EvolutionSection() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-80px" });

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
          <p className="text-sm font-medium uppercase tracking-widest text-emerald-400">Living knowledge</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            Your graph remembers
            <br />
            <span className="text-slate-500">how your knowledge grew.</span>
          </h2>
          <p className="mt-5 text-base leading-relaxed text-slate-400 md:text-lg">
            Every concept added, every connection made — KnowledgeNetwork tracks the evolution
            of your understanding over time with built-in versioning.
          </p>
        </motion.div>

        {/* Timeline visualization */}
        <div className="mx-auto mt-14 grid max-w-4xl gap-6 sm:grid-cols-3">
          {timelineSteps.map((step, i) => (
            <motion.div
              key={step.label}
              className="rounded-2xl border border-white/10 bg-white/[0.03] p-6"
              initial={{ opacity: 0, y: 20 }}
              animate={inView ? { opacity: 1, y: 0 } : {}}
              transition={{ delay: 0.3 + i * 0.15, duration: 0.6 }}
            >
              <div className="flex items-center gap-2">
                <Clock size={14} className="text-emerald-400" />
                <span className="text-xs font-semibold uppercase tracking-wider text-emerald-400">{step.label}</span>
              </div>

              <div className="mt-4 space-y-2">
                {step.nodes.map((node, j) => (
                  <motion.div
                    key={node}
                    className="flex items-center gap-2 rounded-lg border border-white/8 bg-white/[0.03] px-3 py-2 text-xs text-slate-300"
                    initial={{ opacity: 0, x: -8 }}
                    animate={inView ? { opacity: 1, x: 0 } : {}}
                    transition={{ delay: 0.5 + i * 0.15 + j * 0.04 }}
                  >
                    <Plus size={10} className="text-cyan-400" />
                    {node}
                  </motion.div>
                ))}
              </div>

              <div className="mt-4 flex items-center gap-2 text-xs text-slate-500">
                <GitBranch size={12} />
                <span>{step.edges} connections</span>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
