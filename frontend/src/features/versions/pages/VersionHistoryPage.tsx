import { ArrowLeftRight, GitFork, Loader2, Search } from "lucide-react";
import { motion } from "framer-motion";
import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { workspaceApi, versioningApi } from "../../../services";
import { useRestoreVersion, useForkGraph } from "../../../hooks/use-mutations";
import { CompareView } from "../components/CompareView";
import { ForkGraph } from "../components/ForkGraph";
import { RestoreModal } from "../components/RestoreModal";
import { VersionTimeline } from "../components/VersionTimeline";
import type { VersionHistoryState } from "../types";
import type { VersionEntryResponse } from "../../../services";
import { EmptyState } from "../../../components/ui/empty-state";

function mapToVersionCommit(entry: VersionEntryResponse) {
  return {
    id: entry.id,
    hash: entry.snapshot.id.slice(0, 7),
    branch: entry.label,
    message: entry.snapshot.name,
    author: entry.createdBy,
    authorInitials: entry.createdBy.slice(0, 2).toUpperCase(),
    timestamp: entry.createdAt,
    nodesAdded: entry.snapshot.nodeCount,
    nodesRemoved: 0,
    edgesAdded: entry.snapshot.edgeCount,
    edgesRemoved: 0,
    tags: [entry.changeType],
    isMerge: entry.changeType === "merge",
    parentIds: []
  };
}

const initialState: VersionHistoryState = {
  selectedCommitId: null,
  compareSelection: { left: null, right: null },
  isCompareOpen: false,
  isRestoreModalOpen: false,
  restoreCommitId: null
};

export function VersionHistoryPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<VersionHistoryState>(initialState);
  const [query, setQuery] = useState("");
  const [selectedCommitId, setSelectedCommitId] = useState<string | null>(null);

  const { data: workspaces, isLoading: isLoadingWorkspaces } = useQuery({
    queryKey: ["workspaces"],
    queryFn: () => workspaceApi.list().then((r) => r.data)
  });

  const workspaceId = workspaces?.[0]?.id ?? "default-workspace";

  const {
    data: versionData,
    isLoading: isLoadingHistory,
    isError,
    error,
    refetch
  } = useQuery({
    queryKey: ["version-history", workspaceId],
    queryFn: () => versioningApi.listHistory(workspaceId, 0, 20).then((r) => r.data),
    enabled: !!workspaceId
  });

  const versionHistory: VersionEntryResponse[] = versionData?.content ?? [];

  const restoreMutation = useRestoreVersion(workspaceId);
  const forkMutation = useForkGraph(workspaceId);

  const commits = useMemo(() => {
    if (!query.trim()) return versionHistory.map(mapToVersionCommit);
    const normalized = query.trim().toLowerCase();
    return versionHistory
      .map(mapToVersionCommit)
      .filter((commit) =>
        commit.message.toLowerCase().includes(normalized) ||
        commit.author.toLowerCase().includes(normalized) ||
        commit.branch.toLowerCase().includes(normalized) ||
        commit.hash.toLowerCase().includes(normalized) ||
        commit.tags.some((tag: string) => tag.toLowerCase().includes(normalized))
      );
  }, [versionHistory, query]);

  const selectedCommit = selectedCommitId
    ? versionHistory.find((c) => c.id === selectedCommitId) ?? null
    : null;
  const restoreCommit = state.restoreCommitId
    ? versionHistory.find((entry) => entry.id === state.restoreCommitId) ?? null
    : null;

  const handleSelectCommit = (id: string) => {
    setSelectedCommitId(id);
  };

  const handleRequestRestore = (id: string) => {
    setState((prev) => ({ ...prev, isRestoreModalOpen: true, restoreCommitId: id }));
  };

  const handleRequestCompare = (id: string) => {
    setState((prev) => {
      const current = prev.compareSelection;
      const nextLeft = current.left ?? id;
      const nextRight = current.right && current.right !== id ? current.right : id;
      return {
        ...prev,
        compareSelection: { left: nextLeft, right: nextRight },
        isCompareOpen: true
      };
    });
  };

  const handleConfirmRestore = () => {
    setState((prev) => ({ ...prev, isRestoreModalOpen: false, restoreCommitId: null }));
  };

  const handleRestore = () => {
    if (state.restoreCommitId) {
      restoreMutation.mutate(state.restoreCommitId, {
        onSuccess: () => {
          handleConfirmRestore();
          refetch();
        }
      });
    }
  };

  const isLoading = isLoadingWorkspaces || isLoadingHistory;

  if (isLoading) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <motion.div
          className="mb-6 flex flex-wrap items-end justify-between gap-4"
          initial={{ opacity: 0, y: 14 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
        >
          <div className="space-y-2">
            <div className="h-4 w-32 rounded-md bg-white/8 skeleton-shimmer" />
            <div className="h-7 w-64 rounded-md bg-white/8 skeleton-shimmer" />
            <div className="h-4 w-96 rounded-md bg-white/8 skeleton-shimmer" />
          </div>
        </motion.div>
        <div className="flex items-center justify-center py-12 sm:py-20">
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex items-center gap-2 text-sm text-cyan-300"
          >
            <Loader2 size={18} className="animate-spin" />
            Loading version history...
          </motion.div>
        </div>
      </section>
    );
  }

  if (isError) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <EmptyState
          icon={<Search size={28} className="text-rose-300" />}
          title="Unable to load version history"
          description={(error as any)?.message || "Failed to fetch version history."}
        />
      </section>
    );
  }

  return (
    <section className="mx-auto mt-6 max-w-7xl pt-4">
      <motion.div
        className="mb-6 flex flex-wrap items-end justify-between gap-4"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-muted-foreground">Version history</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Graph timeline</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
            Review commits, restore prior snapshots, compare versions, and inspect the fork graph.
          </p>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="mb-6 flex flex-col gap-3 rounded-2xl border border-white/10 bg-[rgba(8,12,20,0.72)] p-3 shadow-glass backdrop-blur-2xl md:flex-row md:items-center md:justify-between"
      >
        <div className="relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search commits by message, author, branch, or hash..."
            className="h-10 w-64 rounded-lg border border-white/10 bg-white/[0.055] pl-9 pr-3 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-cyan-300/50 focus:bg-white/[0.075] focus:ring-4 focus:ring-cyan-300/10"
          />
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => {
              if (versionHistory.length < 2) return;
              const firstVersion = versionHistory[0];
              const secondVersion = versionHistory[1];
              if (!firstVersion || !secondVersion) return;
              const left = selectedCommitId ?? secondVersion.id;
              const right = versionHistory.find((entry) => entry.id !== left)?.id ?? firstVersion.id;
              setState((prev) => ({
                ...prev,
                compareSelection: { left, right },
                isCompareOpen: true
              }));
            }}
            disabled={versionHistory.length < 2}
            className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-xs text-slate-300 transition hover:border-white/16 hover:bg-white/[0.075] disabled:cursor-not-allowed disabled:opacity-40"
          >
            <ArrowLeftRight size={14} />
            Compare
          </button>
          <button
            onClick={() => {
              if (selectedCommitId) {
                const commit = versionHistory.find((c) => c.id === selectedCommitId);
                if (commit) {
                  forkMutation.mutate(
                    { sourceVersionId: commit.id, name: `Fork from ${commit.label}`, description: `Forked from version ${commit.versionNumber}` },
                    {
                      onSuccess: (response) => {
                        forkMutation.reset();
                        navigate(`/graphs/${response.data.workspaceId}`);
                      }
                    }
                  );
                }
              }
            }}
            className="inline-flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-xs text-slate-300 transition hover:border-white/16 hover:bg-white/[0.075]"
          >
            <GitFork size={14} />
            Fork graph
          </button>
        </div>
      </motion.div>

      <div className="grid gap-6 xl:grid-cols-[1fr_320px]">
        <div>
          <VersionTimeline
            commits={commits}
            selectedCommitId={selectedCommitId}
            onSelectCommit={handleSelectCommit}
            onRequestRestore={handleRequestRestore}
            onRequestCompare={handleRequestCompare}
          />
        </div>

        <aside className="space-y-6">
          <ForkGraph
            versionHistory={versionHistory}
            onSelectCommit={handleSelectCommit}
            selectedCommitId={selectedCommitId}
            workspaceId={workspaceId}
          />

          {selectedCommit && (
            <motion.div
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              className="rounded-2xl border border-white/10 bg-white/[0.03] p-4"
            >
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Selected commit</p>
              <p className="mt-2 text-sm font-semibold text-slate-100">{selectedCommit.snapshot.name}</p>
              <div className="mt-2 space-y-1 text-xs text-muted-foreground">
                <p>{selectedCommit.createdBy}</p>
                <p>{selectedCommit.label}</p>
                <p className="font-mono text-[10px] text-slate-500">{selectedCommit.snapshot.id}</p>
              </div>
            </motion.div>
          )}

          {state.isCompareOpen && (
            <CompareView
              leftId={state.compareSelection.left}
              rightId={state.compareSelection.right}
              versionHistory={versionHistory}
              onClose={() =>
                setState((prev) => ({
                  ...prev,
                  isCompareOpen: false,
                  compareSelection: { left: null, right: null }
                }))
              }
              onRestore={(id: string) => {
                setState((prev) => ({
                  ...prev,
                  isCompareOpen: false,
                  compareSelection: { left: null, right: null },
                  isRestoreModalOpen: true,
                  restoreCommitId: id
                }));
              }}
            />
          )}
        </aside>
      </div>

      <RestoreModal
        commit={restoreCommit}
        onClose={() => setState((prev) => ({ ...prev, isRestoreModalOpen: false, restoreCommitId: null }))}
        onConfirm={handleRestore}
      />
    </section>
  );
}