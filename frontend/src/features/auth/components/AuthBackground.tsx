import { motion } from "framer-motion";
import { Network } from "lucide-react";

export function AuthBackground({ children }: { children: React.ReactNode }) {
  return (
    <main className="relative min-h-screen overflow-hidden bg-background text-foreground">
      <div className="absolute inset-0 animated-auth-gradient" />
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.035)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.035)_1px,transparent_1px)] bg-[length:52px_52px]" />
      <motion.div
        className="absolute left-[8%] top-[12%] h-44 w-44 rounded-full border border-cyan-300/20 bg-cyan-300/10 blur-sm"
        animate={{ y: [0, 18, 0], x: [0, 10, 0] }}
        transition={{ duration: 9, repeat: Infinity, ease: "easeInOut" }}
      />
      <motion.div
        className="absolute bottom-[10%] right-[12%] h-56 w-56 rounded-full border border-violet-300/20 bg-violet-400/10 blur-sm"
        animate={{ y: [0, -16, 0], x: [0, -12, 0] }}
        transition={{ duration: 10, repeat: Infinity, ease: "easeInOut" }}
      />

      <div className="relative z-10 grid min-h-screen px-5 py-6 lg:grid-cols-[1fr_520px] lg:px-8">
        <section className="hidden min-h-full flex-col justify-between py-4 lg:flex">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-cyan-400 text-white shadow-glow">
              <Network size={19} />
            </div>
            <div>
              <p className="text-sm font-semibold">KnowledgeNetwork</p>
              <p className="text-xs text-muted-foreground">Collaborative graph intelligence</p>
            </div>
          </div>

          <div className="max-w-2xl pb-12">
            <p className="mb-5 inline-flex rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs text-cyan-100">
              Private beta workspace
            </p>
            <h1 className="text-5xl font-semibold leading-[1.03] tracking-[-0.025em] text-white">
              Map ideas, decisions, and relationships in one living system.
            </h1>
            <p className="mt-5 max-w-xl text-base leading-7 text-slate-300">
              A premium command layer for teams building connected knowledge graphs with versioning, search, and collaborative context.
            </p>
          </div>
        </section>

        <section className="flex items-center justify-center lg:justify-end">{children}</section>
      </div>
    </main>
  );
}
