import { motion, useInView } from "framer-motion";
import { useRef } from "react";
import { BookOpen, Bookmark, FileText, FolderOpen, Lightbulb, StickyNote } from "lucide-react";

const scatteredItems = [
  { label: "Research notes", icon: FileText, x: "5%", y: "10%", rotate: -12 },
  { label: "Bookmarks", icon: Bookmark, x: "72%", y: "5%", rotate: 8 },
  { label: "Ideas", icon: Lightbulb, x: "15%", y: "65%", rotate: -6 },
  { label: "Course notes", icon: BookOpen, x: "60%", y: "70%", rotate: 14 },
  { label: "Documents", icon: FolderOpen, x: "85%", y: "45%", rotate: -10 },
  { label: "Quick thoughts", icon: StickyNote, x: "40%", y: "15%", rotate: 5 },
];

export function ProblemSection() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-100px" });

  return (
    <section className="relative py-24 md:py-36" id="how-it-works" ref={ref}>
      <div className="mx-auto max-w-[1200px] px-6 md:px-10">
        <motion.div
          className="mx-auto max-w-3xl text-center"
          initial={{ opacity: 0, y: 24 }}
          animate={inView ? { opacity: 1, y: 0 } : {}}
          transition={{ duration: 0.7, ease: [0.16, 1, 0.3, 1] }}
        >
          <p className="text-sm font-medium uppercase tracking-widest text-cyan-400">The problem</p>
          <h2 className="mt-4 text-3xl font-bold leading-tight tracking-tight text-white sm:text-4xl md:text-5xl">
            We collect information
            <br />
            <span className="text-slate-500">everywhere.</span>
          </h2>
          <p className="mt-5 text-base leading-relaxed text-slate-400 md:text-lg">
            Notes in one app. Bookmarks in another. Research scattered across documents, courses,
            and conversations. The knowledge is there — but the connections between ideas stay invisible.
          </p>
        </motion.div>

        {/* Scattered items visualization */}
        <div className="relative mx-auto mt-16 h-[320px] max-w-3xl md:h-[400px]">
          {scatteredItems.map((item, i) => {
            const Icon = item.icon;
            return (
              <motion.div
                key={item.label}
                className="absolute"
                style={{ left: item.x, top: item.y }}
                initial={{ opacity: 0, scale: 0.6, rotate: item.rotate * 2 }}
                animate={
                  inView
                    ? { opacity: 1, scale: 1, rotate: item.rotate }
                    : {}
                }
                transition={{ delay: 0.3 + i * 0.1, duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
              >
                <div className="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.04] px-4 py-3 shadow-glass backdrop-blur-xl">
                  <Icon size={16} className="text-slate-500" />
                  <span className="text-sm text-slate-400">{item.label}</span>
                </div>
              </motion.div>
            );
          })}

          {/* Connecting dots hint */}
          <motion.div
            className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2"
            initial={{ opacity: 0 }}
            animate={inView ? { opacity: 1 } : {}}
            transition={{ delay: 1.2, duration: 0.8 }}
          >
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl border border-cyan-400/20 bg-cyan-400/5 text-cyan-400">
              <span className="text-2xl font-bold">?</span>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}
