import { Link } from "react-router-dom";
import { motion, useInView } from "framer-motion";
import { useRef } from "react";
import { ArrowRight } from "lucide-react";

export function FinalCTA() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-80px" });

  return (
    <section className="relative py-28 md:py-40" ref={ref}>
      <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/10 to-transparent" />

      {/* Atmospheric glow */}
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute left-1/2 top-1/2 h-[500px] w-[600px] -translate-x-1/2 -translate-y-1/2 bg-[radial-gradient(ellipse_at_center,rgba(34,211,238,0.05),transparent_60%)]" />
      </div>

      <div className="relative mx-auto max-w-[1200px] px-6 md:px-10">
        <motion.div
          className="mx-auto max-w-3xl text-center"
          initial={{ opacity: 0, y: 24 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        >
          <h2 className="text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl md:text-6xl">
            Start connecting
            <br />
            <span className="bg-gradient-to-r from-cyan-400 to-violet-400 bg-clip-text text-transparent">
              what you know.
            </span>
          </h2>

          <p className="mx-auto mt-6 max-w-xl text-base leading-relaxed text-slate-400 md:text-lg">
            Your first knowledge network is free. No credit card required.
            See how your ideas connect in minutes.
          </p>

          <motion.div
            className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row"
            initial={{ opacity: 0, y: 12 }}
            animate={inView ? { opacity: 1, y: 0 } : {}}
            transition={{ delay: 0.3, duration: 0.5 }}
          >
            <Link
              to="/register"
              className="group inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-cyan-400 px-8 py-4 text-base font-semibold text-slate-950 shadow-glow transition hover:shadow-lg hover:brightness-110"
              id="final-cta-signup"
            >
              Build your first network
              <ArrowRight size={18} className="transition-transform group-hover:translate-x-1" />
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center gap-2 rounded-xl border border-white/12 bg-white/[0.04] px-8 py-4 text-base font-medium text-slate-200 transition hover:border-white/25 hover:bg-white/[0.07]"
            >
              I already have an account
            </Link>
          </motion.div>
        </motion.div>
      </div>
    </section>
  );
}
