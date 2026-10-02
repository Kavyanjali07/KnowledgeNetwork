import { motion, useInView } from "framer-motion";
import { useRef } from "react";
import { Lock, Shield, Users } from "lucide-react";

const roles = [
  { role: "Owner", desc: "Full control over the graph, members, and settings", icon: Shield, color: "text-cyan-400" },
  { role: "Editor", desc: "Create, modify, and connect concepts", icon: Users, color: "text-violet-400" },
  { role: "Viewer", desc: "Explore and traverse the knowledge graph", icon: Lock, color: "text-emerald-400" },
];

export function CollaborationSection() {
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
          <p className="text-sm font-medium uppercase tracking-widest text-cyan-400">Collaborate</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            Build knowledge
            <br />
            <span className="text-slate-500">together.</span>
          </h2>
          <p className="mt-5 text-base leading-relaxed text-slate-400 md:text-lg">
            Invite others to contribute to your knowledge network.
            Workspaces keep your graphs organized with clear roles and permissions.
          </p>
        </motion.div>

        {/* Role cards */}
        <div className="mx-auto mt-14 grid max-w-3xl gap-4 sm:grid-cols-3">
          {roles.map((item, i) => {
            const Icon = item.icon;
            return (
              <motion.div
                key={item.role}
                className="rounded-2xl border border-white/10 bg-white/[0.03] p-6 text-center"
                initial={{ opacity: 0, y: 16 }}
                animate={inView ? { opacity: 1, y: 0 } : {}}
                transition={{ delay: 0.3 + i * 0.1, duration: 0.5 }}
              >
                <div className={`mx-auto flex h-12 w-12 items-center justify-center rounded-xl border border-white/10 bg-white/[0.04] ${item.color}`}>
                  <Icon size={22} />
                </div>
                <h3 className="mt-4 text-base font-semibold text-white">{item.role}</h3>
                <p className="mt-2 text-sm text-slate-500">{item.desc}</p>
              </motion.div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
