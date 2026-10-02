import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { HeroGraph } from "./HeroGraph";
import { ArrowRight, Sparkles } from "lucide-react";

export function HeroSection() {
  return (
    <section className="relative min-h-screen overflow-hidden pt-32 pb-20 md:pt-40 md:pb-28">
      {/* Atmospheric background */}
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute left-1/2 top-0 h-[600px] w-[900px] -translate-x-1/2 bg-[radial-gradient(ellipse_at_center,rgba(34,211,238,0.06),transparent_60%)]" />
        <div className="absolute right-0 top-1/4 h-[400px] w-[400px] bg-[radial-gradient(circle,rgba(124,58,237,0.06),transparent_60%)]" />
      </div>

      <div className="relative mx-auto max-w-[1200px] px-6 md:px-10">
        {/* Headline */}
        <motion.div
          className="mx-auto max-w-4xl text-center"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        >
          <motion.div
            className="mb-6 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-4 py-1.5 text-xs font-medium text-slate-400"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Sparkles size={12} className="text-cyan-400" />
            Visual knowledge management
          </motion.div>

          <h1 className="text-5xl font-extrabold leading-[1.05] tracking-tight text-white sm:text-6xl md:text-7xl lg:text-8xl">
            Your knowledge
            <br />
            <span className="bg-gradient-to-r from-cyan-400 via-cyan-300 to-violet-400 bg-clip-text text-transparent">
              is a network.
            </span>
          </h1>

          <motion.p
            className="mx-auto mt-6 max-w-2xl text-base leading-relaxed text-slate-400 sm:text-lg md:mt-8 md:text-xl"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4, duration: 0.6 }}
          >
            Turn scattered ideas, research, and concepts into a connected knowledge graph.
            See relationships between everything you know.
          </motion.p>

          {/* CTAs */}
          <motion.div
            className="mt-8 flex flex-col items-center justify-center gap-4 sm:flex-row md:mt-10"
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6, duration: 0.5 }}
          >
            <Link
              to="/register"
              className="group inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-cyan-400 px-7 py-3.5 text-base font-semibold text-slate-950 shadow-glow transition hover:shadow-lg hover:brightness-110"
              id="hero-cta-signup"
            >
              Build your knowledge network
              <ArrowRight size={18} className="transition-transform group-hover:translate-x-1" />
            </Link>
            <a
              href="#explore"
              onClick={(e) => {
                e.preventDefault();
                document.querySelector("#explore")?.scrollIntoView({ behavior: "smooth" });
              }}
              className="inline-flex items-center gap-2 rounded-xl border border-white/12 bg-white/[0.04] px-7 py-3.5 text-base font-medium text-slate-200 transition hover:border-white/25 hover:bg-white/[0.07]"
              id="hero-cta-explore"
            >
              Explore an example
            </a>
          </motion.div>
        </motion.div>

        {/* Hero Graph */}
        <div className="mt-16 md:mt-20" id="explore">
          <HeroGraph />
        </div>
      </div>
    </section>
  );
}
