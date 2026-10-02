import { Loader2, Search } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect } from "react";
import { useAdvancedSearch } from "../hooks/useAdvancedSearch";
import { useGlobalSearch } from "../hooks/useGlobalSearch";

export function SearchPage() {
  const { isSearchLoading } = useAdvancedSearch();
  const { openSearch } = useGlobalSearch();

  useEffect(() => {
    openSearch();
  }, [openSearch]);

  return (
    <section className="mx-auto mt-6 max-w-4xl pt-4">
      <motion.div
        className="mb-6 flex flex-wrap items-end justify-between gap-4"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-cyan-400">Search & Explore</p>
          <h1 className="mt-1 text-2xl font-bold tracking-tight text-white md:text-4xl">Knowledge search</h1>
          <p className="mt-3 max-w-xl text-sm leading-relaxed text-slate-400">
            Search concepts, relationships, networks, and commands across all your authorized workspaces. Use keyboard shortcuts to navigate instantly.
          </p>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05, duration: 0.28 }}
        className="mb-4 flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-3 text-xs sm:text-sm text-muted-foreground md:px-4"
      >
        <Search size={14} className="text-cyan-400 sm:hidden" />
        <Search size={16} className="text-cyan-400 hidden sm:block" />
        Press{" "}
        <kbd className="rounded border border-white/10 bg-white/10 px-1 font-mono text-[10px] sm:text-[11px] text-slate-200">
          ⌘K
        </kbd>{" "}
        to open search anywhere, or click the search bar in the top navigation.
      </motion.div>

      <div className="relative">
        {isSearchLoading && (
          <div className="flex items-center justify-center py-12 sm:py-20">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="flex items-center gap-2 text-sm text-cyan-300"
            >
              <Loader2 size={18} className="animate-spin" />
              Searching...
            </motion.div>
          </div>
        )}
      </div>
    </section>
  );
}
