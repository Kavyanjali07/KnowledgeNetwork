import { motion } from "framer-motion";
import { useQuery } from "@tanstack/react-query";
import { useNavigate, Link } from "react-router-dom";
import {
  ArrowRight,
  Eye,
  GitBranch,
  Lock,
  Network,
  Plus,
  Search,
  Sparkles,
} from "lucide-react";
import { graphApi } from "../../services";
import { useAuth } from "../../lib/auth-context";
import { GraphCard } from "../../features/graphs/components/GraphCard";
import { mapGraphResponseToManagedGraph } from "../../features/graphs/data/graphs";

const fadeIn = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0 }
};

export function DashboardPreview() {
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const graphsQuery = useQuery({
    queryKey: ["dashboard-graphs"],
    queryFn: () => graphApi.list({ page: 0, size: 6 }).then((res) => res.data)
  });

  const rawGraphs = graphsQuery.data?.content ?? [];
  const totalGraphs = graphsQuery.data?.totalElements ?? 0;

  const totalNodes = rawGraphs.reduce((acc, g) => acc + (g.nodeCount ?? 0), 0);
  const totalEdges = rawGraphs.reduce((acc, g) => acc + (g.edgeCount ?? 0), 0);
  const publicCount = rawGraphs.filter((g) => g.visibility === "PUBLIC").length;

  const managedGraphs = rawGraphs.map(mapGraphResponseToManagedGraph);

  const greeting = currentUser?.firstName
    ? `Welcome back, ${currentUser.firstName}`
    : "Welcome to KnowledgeNetwork";

  return (
    <motion.section
      className="mx-auto mt-6 max-w-7xl pt-4"
      initial="hidden"
      animate="visible"
      variants={{
        hidden: {},
        visible: { transition: { staggerChildren: 0.07 } }
      }}
    >
      {/* Header */}
      <motion.div
        variants={fadeIn}
        className="mb-8 flex flex-wrap items-end justify-between gap-4"
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm font-medium text-cyan-400">Knowledge Home</p>
          <h1 className="mt-2 text-2xl font-bold tracking-tight text-white md:text-4xl">{greeting}</h1>
          <p className="mt-2 max-w-2xl text-sm leading-relaxed text-slate-400">
            Map, connect, and traverse your knowledge graphs in one living system.
          </p>
        </div>

        <button
          onClick={() => navigate("/graphs")}
          className="inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-cyan-400 px-5 py-2.5 text-sm font-semibold text-slate-950 shadow-glow transition hover:brightness-110"
        >
          <Plus size={16} />
          Build new network
        </button>
      </motion.div>

      {/* Honest Real Metrics */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <motion.div
          variants={fadeIn}
          className="rounded-xl border border-white/10 bg-white/[0.035] p-5 shadow-glass backdrop-blur-xl"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Networks</span>
            <Network size={18} className="text-cyan-400" />
          </div>
          <p className="mt-3 text-3xl font-bold text-white">{totalGraphs}</p>
          <p className="mt-1 text-xs text-slate-500">{publicCount} public, {totalGraphs - publicCount} private</p>
        </motion.div>

        <motion.div
          variants={fadeIn}
          className="rounded-xl border border-white/10 bg-white/[0.035] p-5 shadow-glass backdrop-blur-xl"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Concepts</span>
            <Sparkles size={18} className="text-violet-400" />
          </div>
          <p className="mt-3 text-3xl font-bold text-white">{totalNodes}</p>
          <p className="mt-1 text-xs text-slate-500">Across your networks</p>
        </motion.div>

        <motion.div
          variants={fadeIn}
          className="rounded-xl border border-white/10 bg-white/[0.035] p-5 shadow-glass backdrop-blur-xl"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Relationships</span>
            <GitBranch size={18} className="text-emerald-400" />
          </div>
          <p className="mt-3 text-3xl font-bold text-white">{totalEdges}</p>
          <p className="mt-1 text-xs text-slate-500">Semantic connections authored</p>
        </motion.div>

        <motion.div
          variants={fadeIn}
          className="rounded-xl border border-white/10 bg-white/[0.035] p-5 shadow-glass backdrop-blur-xl"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Visibility</span>
            <Eye size={18} className="text-amber-400" />
          </div>
          <p className="mt-3 text-3xl font-bold text-white">{publicCount > 0 ? "Public & Private" : "Private Only"}</p>
          <p className="mt-1 text-xs text-slate-500">Configured via network settings</p>
        </motion.div>
      </div>

      {/* Main Content Area */}
      <div className="mt-8 space-y-6">
        <div className="flex items-center justify-between border-b border-white/10 pb-4">
          <div>
            <h2 className="text-lg font-semibold text-white">Your Knowledge Networks</h2>
            <p className="text-xs text-slate-400">Recent graphs you have created or edited</p>
          </div>
          <Link
            to="/graphs"
            className="flex items-center gap-1 text-xs font-medium text-cyan-400 hover:text-cyan-300 transition"
          >
            View all ({totalGraphs}) <ArrowRight size={14} />
          </Link>
        </div>

        {graphsQuery.isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-64 rounded-2xl border border-white/10 bg-white/[0.03] p-4 animate-pulse" />
            ))}
          </div>
        ) : managedGraphs.length > 0 ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {managedGraphs.map((graph) => (
              <GraphCard key={graph.id} graph={graph} />
            ))}
          </div>
        ) : (
          /* Honest Empty State for new users */
          <motion.div
            variants={fadeIn}
            className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-white/14 bg-white/[0.02] p-12 text-center"
          >
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-cyan-400/30 bg-cyan-400/10 text-cyan-400 mb-4">
              <Network size={28} />
            </div>
            <h3 className="text-lg font-semibold text-white">No knowledge networks built yet</h3>
            <p className="mt-2 max-w-md text-sm text-slate-400">
              Start by creating your first network. Map out concepts, artifacts, and decisions on an infinite canvas.
            </p>
            <button
              onClick={() => navigate("/graphs")}
              className="mt-6 inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-cyan-500 to-cyan-400 px-6 py-3 text-sm font-semibold text-slate-950 shadow-glow transition hover:brightness-110"
            >
              <Plus size={16} />
              Build your first network
            </button>
          </motion.div>
        )}
      </div>
    </motion.section>
  );
}
