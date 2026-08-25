import { memo } from "react";
import { motion } from "framer-motion";
import {
  Activity,
  ArrowUpRight,
  BrainCircuit,
  Clock3,
  FilePlus2,
  GitBranch,
  Layers3,
  Network,
  Search,
  Share2,
  ShieldCheck,
  Sparkles,
  Zap
} from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../ui/button";

const analytics = [
  {
    label: "Active nodes",
    value: "12,840",
    change: "+18.4%",
    detail: "1,204 enriched this week",
    icon: Network,
    gradient: "from-cyan-300/22 to-violet-400/10",
    series: [34, 42, 39, 54, 58, 73, 82, 78, 91]
  },
  {
    label: "Relationships",
    value: "48,219",
    change: "+9.7%",
    detail: "Confidence avg. 92%",
    icon: GitBranch,
    gradient: "from-violet-300/22 to-fuchsia-400/10",
    series: [26, 31, 40, 38, 52, 56, 61, 70, 76]
  },
  {
    label: "Team activity",
    value: "386",
    change: "+31.2%",
    detail: "Across 14 collaborators",
    icon: Activity,
    gradient: "from-emerald-300/20 to-cyan-400/10",
    series: [18, 24, 21, 32, 47, 44, 58, 62, 71]
  },
  {
    label: "Graph health",
    value: "99.3%",
    change: "+2.1%",
    detail: "No orphan clusters",
    icon: ShieldCheck,
    gradient: "from-amber-300/18 to-cyan-400/10",
    series: [72, 74, 73, 78, 77, 82, 88, 91, 94]
  }
];

const activity = [
  { actor: "Maya", action: "linked Roadmap to Customer Signals", time: "2m", icon: GitBranch },
  { actor: "Ari", action: "published Ontology v18", time: "8m", icon: Layers3 },
  { actor: "AI", action: "suggested 12 duplicate concepts", time: "14m", icon: Sparkles },
  { actor: "Kavya", action: "created Market Map cluster", time: "22m", icon: Network }
];

const popularGraphs = [
  { name: "Product Intelligence", nodes: "12.8k", trend: "+18%", hue: "bg-cyan-300", progress: 92 },
  { name: "Customer Signals", nodes: "8.4k", trend: "+11%", hue: "bg-violet-300", progress: 78 },
  { name: "Research Memory", nodes: "6.1k", trend: "+8%", hue: "bg-emerald-300", progress: 64 },
  { name: "Decision Archive", nodes: "4.9k", trend: "+5%", hue: "bg-amber-300", progress: 52 }
];

const quickActions = [
  { label: "Create node", icon: FilePlus2 },
  { label: "Ask graph AI", icon: BrainCircuit },
  { label: "Search clusters", icon: Search },
  { label: "Share workspace", icon: Share2 }
];

const chartData = [
  { label: "Mon", nodes: 42, edges: 28 },
  { label: "Tue", nodes: 58, edges: 36 },
  { label: "Wed", nodes: 52, edges: 44 },
  { label: "Thu", nodes: 74, edges: 51 },
  { label: "Fri", nodes: 88, edges: 67 },
  { label: "Sat", nodes: 76, edges: 62 },
  { label: "Sun", nodes: 96, edges: 74 }
];

const fadeIn = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0 }
};

function Sparkline({ data }: { data: number[] }) {
  const points = useMemo(() => {
    const max = Math.max(...data);
    const min = Math.min(...data);
    return data
      .map((value, index) => {
        const x = (index / (data.length - 1)) * 120;
        const y = 42 - ((value - min) / Math.max(max - min, 1)) * 34;
        return `${x},${y}`;
      })
      .join(" ");
  }, [data]);

  return (
    <svg viewBox="0 0 120 46" className="h-12 w-28 overflow-visible">
      <motion.polyline
        points={points}
        fill="none"
        stroke="url(#sparkline)"
        strokeWidth="3"
        strokeLinecap="round"
        strokeLinejoin="round"
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={{ duration: 0.9, ease: "easeOut" }}
      />
      <defs>
        <linearGradient id="sparkline" x1="0" x2="1">
          <stop offset="0%" stopColor="#a78bfa" />
          <stop offset="100%" stopColor="#22d3ee" />
        </linearGradient>
      </defs>
    </svg>
  );
}

export const AnalyticsCard = memo(function AnalyticsCard({ item, index }: { item: (typeof analytics)[number]; index: number }) {
  return (
    <motion.article
      variants={fadeIn}
      transition={{ delay: index * 0.05, duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
      whileHover={{ y: -4, scale: 1.01 }}
      className="group relative overflow-hidden rounded-xl border border-white/10 bg-white/[0.045] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className={`absolute inset-0 bg-gradient-to-br ${item.gradient} opacity-80 transition group-hover:opacity-100`} />
      <div className="relative">
        <div className="flex items-start justify-between gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-white/10 bg-black/20 text-cyan-200">
            <item.icon size={18} />
          </div>
          <span className="rounded-full border border-emerald-300/20 bg-emerald-300/10 px-2 py-1 text-xs text-emerald-100">
            {item.change}
          </span>
        </div>
        <p className="mt-5 text-sm text-muted-foreground">{item.label}</p>
        <div className="mt-2 flex items-end justify-between gap-4">
          <div>
            <p className="text-3xl font-semibold tracking-[-0.02em]">{item.value}</p>
            <p className="mt-1 text-xs text-muted-foreground">{item.detail}</p>
          </div>
          <Sparkline data={item.series} />
        </div>
      </div>
    </motion.article>
  );
});

function GrowthChart() {
  const [activeIndex, setActiveIndex] = useState(4);
  const active = chartData[activeIndex];

  if (!active) return null;

  return (
    <motion.section
      variants={fadeIn}
      className="rounded-xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm text-muted-foreground">Graph growth</p>
          <h2 className="mt-1 text-lg font-semibold">Weekly expansion velocity</h2>
        </div>
        <div className="rounded-lg border border-white/10 bg-black/18 px-3 py-2 text-right">
          <p className="text-xs text-muted-foreground">{active.label}</p>
          <p className="text-sm font-medium">{active.nodes} nodes / {active.edges} edges</p>
        </div>
      </div>

      <div className="mt-8 flex h-64 items-end gap-3 sm:gap-4">
        {chartData.map((item, index) => {
          const selected = index === activeIndex;

          return (
            <button
              key={item.label}
              className="group flex h-full min-w-0 flex-1 flex-col justify-end gap-2"
              onMouseEnter={() => setActiveIndex(index)}
              onFocus={() => setActiveIndex(index)}
            >
              <div className="flex h-full w-full items-end gap-1.5 rounded-lg border border-white/8 bg-black/16 p-1.5 transition group-hover:border-cyan-300/30">
                <motion.div
                  className="flex-1 rounded-md bg-gradient-to-t from-violet-500/70 to-cyan-300"
                  initial={{ height: 0 }}
                  animate={{ height: `${item.nodes}%` }}
                  transition={{ duration: 0.55, delay: index * 0.04, ease: [0.16, 1, 0.3, 1] }}
                />
                <motion.div
                  className="flex-1 rounded-md bg-white/18"
                  initial={{ height: 0 }}
                  animate={{ height: `${item.edges}%` }}
                  transition={{ duration: 0.55, delay: index * 0.04 + 0.04, ease: [0.16, 1, 0.3, 1] }}
                />
              </div>
              <span className={selected ? "text-xs text-cyan-100" : "text-xs text-muted-foreground"}>{item.label}</span>
            </button>
          );
        })}
      </div>
    </motion.section>
  );
}

function PopularGraphs() {
  return (
    <motion.section
      variants={fadeIn}
      className="rounded-xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-foreground">Popular graphs</p>
          <h2 className="mt-1 text-lg font-semibold">Most visited spaces</h2>
        </div>
        <ArrowUpRight size={18} className="text-muted-foreground" />
      </div>
      <div className="mt-5 space-y-3">
        {popularGraphs.map((graph, index) => (
          <motion.button
            key={graph.name}
            className="w-full rounded-lg border border-white/10 bg-black/16 p-3 text-left transition hover:border-cyan-300/25 hover:bg-white/[0.065]"
            initial={{ opacity: 0, x: 12 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.15 + index * 0.05 }}
          >
            <div className="flex items-center gap-3">
              <span className={`h-2.5 w-2.5 rounded-full ${graph.hue} shadow-glow`} />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">{graph.name}</p>
                <p className="mt-1 text-xs text-muted-foreground">{graph.nodes} nodes</p>
              </div>
              <span className="text-xs text-emerald-200">{graph.trend}</span>
            </div>
            <div className="mt-3 h-1.5 rounded-full bg-white/8">
              <motion.div
                className="h-full rounded-full bg-gradient-to-r from-violet-400 to-cyan-300"
                initial={{ width: 0 }}
                animate={{ width: `${graph.progress}%` }}
                transition={{ duration: 0.65, delay: 0.2 + index * 0.05 }}
              />
            </div>
          </motion.button>
        ))}
      </div>
    </motion.section>
  );
}

function RecentActivity() {
  return (
    <motion.section
      variants={fadeIn}
      className="rounded-xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-foreground">Recent activity</p>
          <h2 className="mt-1 text-lg font-semibold">Live workspace pulse</h2>
        </div>
        <Clock3 size={18} className="text-muted-foreground" />
      </div>
      <div className="mt-5 space-y-3">
        {activity.map((item, index) => (
          <motion.div
            key={`${item.actor}-${item.action}`}
            className="flex gap-3 rounded-lg border border-white/10 bg-black/16 p-3"
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 + index * 0.06 }}
          >
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border border-white/10 bg-white/[0.055] text-cyan-200">
              <item.icon size={16} />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm leading-5">
                <span className="font-medium">{item.actor}</span> {item.action}
              </p>
              <p className="mt-1 text-xs text-muted-foreground">{item.time} ago</p>
            </div>
          </motion.div>
        ))}
      </div>
    </motion.section>
  );
}

function QuickActions() {
  return (
    <motion.section
      variants={fadeIn}
      className="rounded-xl border border-white/10 bg-white/[0.04] p-4 shadow-glass backdrop-blur-xl"
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-foreground">Quick actions</p>
          <h2 className="mt-1 text-lg font-semibold">Move fast</h2>
        </div>
        <Zap size={18} className="text-cyan-200" />
      </div>
      <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
        {quickActions.map((action, index) => (
          <motion.div key={action.label} whileHover={{ x: 3 }} transition={{ duration: 0.15 }}>
            <Button variant={index === 1 ? "primary" : "secondary"} className="h-12 w-full justify-start">
              <action.icon size={17} />
              {action.label}
            </Button>
          </motion.div>
        ))}
      </div>
    </motion.section>
  );
}

function LoadingSkeletons() {
  return (
    <motion.section variants={fadeIn} className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="overflow-hidden rounded-xl border border-white/10 bg-white/[0.035] p-4">
          <div className="h-10 w-10 rounded-lg bg-white/8 skeleton-shimmer" />
          <div className="mt-5 h-7 w-24 rounded-md bg-white/8 skeleton-shimmer" />
          <div className="mt-3 h-3 w-36 rounded-md bg-white/8 skeleton-shimmer" />
        </div>
      ))}
    </motion.section>
  );
}

export function DashboardPreview() {
  return (
    <motion.section
      className="mx-auto mt-6 max-w-7xl pt-4"
      initial="hidden"
      animate="visible"
      variants={{
        hidden: {},
        visible: {
          transition: {
            staggerChildren: 0.07
          }
        }
      }}
    >
      <motion.div
        variants={fadeIn}
        className="mb-6 flex flex-wrap items-end justify-between gap-4"
        transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-muted-foreground">Workspace overview</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Product Intelligence Graph</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
            Monitor graph growth, team momentum, knowledge quality, and the spaces your collaborators rely on most.
          </p>
        </div>
        <div className="rounded-full border border-cyan-300/20 bg-cyan-300/10 px-3 py-1 text-xs text-cyan-100">
          Live sync active
        </div>
      </motion.div>

      <div className="space-y-4">
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {analytics.map((item, index) => (
            <AnalyticsCard key={item.label} item={item} index={index} />
          ))}
        </div>

        <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
          <div className="space-y-4">
            <GrowthChart />
            <LoadingSkeletons />
          </div>
          <div className="space-y-4">
            <QuickActions />
            <PopularGraphs />
            <RecentActivity />
          </div>
        </div>
      </div>
    </motion.section>
  );
}
