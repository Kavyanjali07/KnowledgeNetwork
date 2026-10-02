import { AnimatePresence, motion } from "framer-motion";
import { Eye, Filter, Grid3X3, Lock, Loader2, Plus, Search, SlidersHorizontal, Trash2, X } from "lucide-react";
import { useMemo, useState } from "react";
import { Button } from "../../../components/ui/button";
import { EmptyState } from "../../../components/ui/empty-state";
import { ErrorPage } from "../../../components/ui/error-page";
import { Input } from "../../../components/ui/input";
import { Skeleton } from "../../../components/ui/skeleton";
import type { ManagedGraph } from "../types";
import { GraphCard } from "../components/GraphCard";
import { mapGraphResponseToManagedGraph } from "../data/graphs";
import { useGraphs } from "../../../hooks/use-queries";
import { useCreateGraph, useUpdateGraph, useDeleteGraph } from "../../../hooks/use-mutations";
import { LicenseSelector } from "../../graph/components/LicenseSelector";
import type { LicenseType } from "../../../services/graphApi";

type SortMode = "updated" | "likes" | "forks" | "nodes";
type VisibilityFilter = "All" | "Public" | "Private";

const sortLabels: Array<{ label: string; value: SortMode }> = [
  { label: "Recently updated", value: "updated" },
  { label: "Most liked", value: "likes" },
  { label: "Most forked", value: "forks" },
  { label: "Most nodes", value: "nodes" }
];

const visibilityFilters: VisibilityFilter[] = ["All", "Public", "Private"];

function sortGraphs(graphs: ManagedGraph[], sort: SortMode) {
  return graphs.toSorted((a: ManagedGraph, b: ManagedGraph) => {
    if (sort === "updated") return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    if (sort === "likes") return b.likes - a.likes;
    if (sort === "forks") return b.forks - a.forks;
    return b.nodes - a.nodes;
  });
}

export function GraphManagementPage() {
  const [query, setQuery] = useState("");
  const [sort, setSort] = useState<SortMode>("updated");
  const [visibility, setVisibility] = useState<VisibilityFilter>("All");

  // Modal states
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingGraph, setEditingGraph] = useState<ManagedGraph | null>(null);
  const [deletingGraph, setDeletingGraph] = useState<ManagedGraph | null>(null);

  // Form states
  const [formTitle, setFormTitle] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formVisibility, setFormVisibility] = useState<"PUBLIC" | "PRIVATE">("PRIVATE");
  const [formLicenseType, setFormLicenseType] = useState<LicenseType>("CC_BY_4_0");
  const [formError, setFormError] = useState<string | null>(null);

  // Mutations
  const createGraph = useCreateGraph();
  const updateGraph = useUpdateGraph();
  const deleteGraph = useDeleteGraph();

  // Fetch real graphs from backend GET /api/v1/graphs
  const {
    data: graphPage,
    isLoading,
    isError,
    error,
    refetch
  } = useGraphs({
    page: 0,
    size: 50,
    sort: sort === "updated" ? "updatedAt" : "name",
    direction: "desc",
    visibility: visibility === "All" ? undefined : visibility.toUpperCase()
  });

  const rawGraphs = graphPage?.content || [];
  const graphs = useMemo(() => rawGraphs.map(mapGraphResponseToManagedGraph), [rawGraphs]);

  const filteredGraphs = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    const filtered = graphs.filter((graph) => {
      const matchesQuery =
        !normalizedQuery ||
        graph.title.toLowerCase().includes(normalizedQuery) ||
        graph.description.toLowerCase().includes(normalizedQuery) ||
        graph.owner.name.toLowerCase().includes(normalizedQuery) ||
        graph.tags.some((tag) => tag.toLowerCase().includes(normalizedQuery));
      const matchesVisibility = visibility === "All" || graph.visibility === visibility;
      return matchesQuery && matchesVisibility;
    });
    return sortGraphs(filtered, sort);
  }, [graphs, query, sort, visibility]);

  const openCreateModal = () => {
    setFormTitle("");
    setFormDescription("");
    setFormVisibility("PRIVATE");
    setFormLicenseType("CC_BY_4_0");
    setFormError(null);
    setIsCreateOpen(true);
  };

  const openEditModal = (graph: ManagedGraph) => {
    setEditingGraph(graph);
    setFormTitle(graph.title);
    setFormDescription(graph.description);
    setFormVisibility(graph.rawVisibility || (graph.visibility === "Public" ? "PUBLIC" : "PRIVATE"));
    setFormLicenseType(graph.licenseType || "CC_BY_4_0");
    setFormError(null);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formTitle.trim()) {
      setFormError("Title is required");
      return;
    }
    setFormError(null);
    try {
      await createGraph.mutateAsync({
        title: formTitle.trim(),
        description: formDescription.trim(),
        visibility: formVisibility,
        licenseType: formVisibility === "PUBLIC" ? formLicenseType : undefined
      });
      setIsCreateOpen(false);
    } catch (err: any) {
      setFormError(err.message || "Failed to create graph");
    }
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingGraph) return;
    if (!formTitle.trim()) {
      setFormError("Title is required");
      return;
    }
    setFormError(null);
    try {
      await updateGraph.mutateAsync({
        id: editingGraph.id,
        title: formTitle.trim(),
        description: formDescription.trim(),
        visibility: formVisibility,
        version: editingGraph.version
      });
      setEditingGraph(null);
    } catch (err: any) {
      setFormError(err.message || "Failed to update graph");
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGraph) return;
    try {
      await deleteGraph.mutateAsync(deletingGraph.id);
      setDeletingGraph(null);
    } catch (err: any) {
      setFormError(err.message || "Failed to delete graph");
    }
  };

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
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-7 w-64" />
            <Skeleton className="h-4 w-96" />
          </div>
          <Skeleton className="h-10 w-32" />
        </motion.div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="overflow-hidden rounded-2xl border border-white/10 bg-white/[0.035] p-4">
              <Skeleton className="h-44 w-full" />
              <Skeleton className="mt-4 h-5 w-3/4" />
              <Skeleton className="mt-2 h-3 w-full" />
              <Skeleton className="mt-4 h-8 w-full" />
            </div>
          ))}
        </div>
      </section>
    );
  }

  if (isError) {
    return (
      <section className="mx-auto mt-6 max-w-7xl pt-4">
        <ErrorPage
          title="Unable to load graphs."
          description={(error as any)?.message || "Failed to fetch knowledge graphs from the server."}
          onRetry={() => refetch()}
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
          <p className="text-sm text-muted-foreground">Graph management</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Knowledge graph library</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
            Create a connected body of knowledge you can explore, evolve, and optionally publish.
          </p>
        </div>
        <Button className="h-10 gap-2 bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400" onClick={openCreateModal}>
          <Plus size={18} />
          Create Network
        </Button>
      </motion.div>

      <motion.div
        className="sticky top-[88px] z-20 mb-6 rounded-2xl border border-white/10 bg-[rgba(8,12,20,0.72)] p-3 shadow-glass backdrop-blur-2xl"
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
      >
        <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_auto_auto]">
          <div className="relative">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search graphs, owners, tags..."
              className="pl-9"
            />
          </div>

          <div className="flex min-w-0 items-center gap-2 overflow-x-auto">
            <span className="hidden items-center gap-2 text-xs text-muted-foreground sm:flex">
              <Filter size={14} />
              Visibility
            </span>
            {visibilityFilters.map((item) => (
              <button
                key={item}
                className={
                  visibility === item
                    ? "shrink-0 rounded-lg border border-cyan-300/25 bg-cyan-300/12 px-3 py-2 text-sm font-medium text-cyan-100"
                    : "shrink-0 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-sm text-muted-foreground transition hover:bg-white/[0.075] hover:text-foreground"
                }
                onClick={() => setVisibility(item)}
              >
                {item}
              </button>
            ))}
          </div>

          <label className="flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3">
            <SlidersHorizontal size={15} className="text-muted-foreground" />
            <select
              value={sort}
              onChange={(event) => setSort(event.target.value as SortMode)}
              className="h-11 bg-transparent text-sm outline-none"
            >
              {sortLabels.map((item) => (
                <option key={item.value} value={item.value} className="bg-slate-950 text-slate-100">
                  {item.label}
                </option>
              ))}
            </select>
          </label>
        </div>
      </motion.div>

      <div className="mb-4 flex items-center justify-between text-sm text-muted-foreground">
        <span>{filteredGraphs.length} graphs</span>
        <span className="flex items-center gap-2">
          <Grid3X3 size={15} />
          Masonry view
        </span>
      </div>

      {graphs.length === 0 ? (
        <EmptyState
          icon={<Grid3X3 size={28} className="text-cyan-200" />}
          title="No graphs yet."
          description="Create your first knowledge graph workspace to start connecting nodes, ideas, and decisions."
          action={
            <Button onClick={openCreateModal} className="bg-cyan-500 text-slate-950 hover:bg-cyan-400 font-semibold">
              <Plus size={16} />
              Create your first graph
            </Button>
          }
        />
      ) : filteredGraphs.length > 0 ? (
        <motion.div layout className="columns-1 gap-4 sm:columns-2 xl:columns-3">
          <AnimatePresence>
            {filteredGraphs.map((graph: ManagedGraph) => (
              <GraphCard
                key={graph.id}
                graph={graph}
                onEdit={openEditModal}
                onDelete={(g) => setDeletingGraph(g)}
              />
            ))}
          </AnimatePresence>
        </motion.div>
      ) : (
        <EmptyState
          icon={<Search size={28} className="text-cyan-200" />}
          title="No graphs found"
          description="Adjust your search, visibility filter, or sorting mode to find the graph space you need."
        />
      )}

      {/* CREATE GRAPH MODAL */}
      <AnimatePresence>
        {isCreateOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-2xl border border-white/10 bg-[rgba(10,15,28,0.96)] p-6 shadow-2xl backdrop-blur-2xl"
            >
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <div>
                  <h3 className="text-xl font-semibold text-slate-100">Create Knowledge Network</h3>
                  <p className="text-xs text-muted-foreground mt-1">Create a connected body of knowledge you can explore, evolve, and optionally publish.</p>
                </div>
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="rounded-lg p-1 text-muted-foreground hover:bg-white/10 hover:text-white"
                >
                  <X size={20} />
                </button>
              </div>

              <form onSubmit={handleCreateSubmit} className="mt-4 space-y-4">
                {formError && (
                  <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-300">
                    {formError}
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Network Title <span className="text-rose-400">*</span>
                  </label>
                  <Input
                    value={formTitle}
                    onChange={(e) => setFormTitle(e.target.value)}
                    placeholder="e.g. Distributed Consensus Systems"
                    className="mt-1.5"
                    maxLength={150}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Description / Scope
                  </label>
                  <textarea
                    value={formDescription}
                    onChange={(e) => setFormDescription(e.target.value)}
                    placeholder="Describe what concepts or relationships this network will contain..."
                    rows={3}
                    maxLength={2000}
                    className="mt-1.5 w-full rounded-xl border border-white/10 bg-white/[0.04] p-3 text-sm text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                    Network Access & Visibility
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <button
                      type="button"
                      onClick={() => setFormVisibility("PRIVATE")}
                      className={`flex flex-col items-start rounded-xl border p-3.5 text-left transition ${
                        formVisibility === "PRIVATE"
                          ? "border-cyan-400/50 bg-cyan-400/10 text-cyan-100 shadow-sm"
                          : "border-white/10 bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
                      }`}
                    >
                      <div className="flex items-center gap-2 font-semibold text-sm text-slate-100">
                        <Lock size={16} className={formVisibility === "PRIVATE" ? "text-cyan-400" : "text-slate-400"} />
                        Private Network
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground leading-snug">Only you and people you explicitly share the workspace with can access this Network.</p>
                    </button>

                    <button
                      type="button"
                      onClick={() => setFormVisibility("PUBLIC")}
                      className={`flex flex-col items-start rounded-xl border p-3.5 text-left transition ${
                        formVisibility === "PUBLIC"
                          ? "border-cyan-400/50 bg-cyan-400/10 text-cyan-100 shadow-sm"
                          : "border-white/10 bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
                      }`}
                    >
                      <div className="flex items-center gap-2 font-semibold text-sm text-slate-100">
                        <Eye size={16} className={formVisibility === "PUBLIC" ? "text-cyan-400" : "text-slate-400"} />
                        Public Network
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground leading-snug">Publish this Network so others can explore it.</p>
                    </button>
                  </div>
                </div>

                {formVisibility === "PUBLIC" && (
                  <div className="pt-2 border-t border-white/10">
                    <LicenseSelector
                      value={formLicenseType}
                      onChange={setFormLicenseType}
                    />
                  </div>
                )}

                <div className="mt-6 flex justify-end gap-3 border-t border-white/10 pt-4">
                  <Button type="button" variant="ghost" onClick={() => setIsCreateOpen(false)}>
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    disabled={createGraph.isPending}
                    className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-2"
                  >
                    {createGraph.isPending && <Loader2 size={16} className="animate-spin" />}
                    Create Network
                  </Button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* EDIT GRAPH MODAL */}
      <AnimatePresence>
        {editingGraph && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="w-full max-w-lg overflow-hidden rounded-2xl border border-white/10 bg-[rgba(10,15,28,0.96)] p-6 shadow-2xl backdrop-blur-2xl"
            >
              <div className="flex items-center justify-between border-b border-white/10 pb-4">
                <h3 className="text-xl font-semibold text-slate-100">Edit Graph Metadata</h3>
                <button
                  type="button"
                  onClick={() => setEditingGraph(null)}
                  className="rounded-lg p-1 text-muted-foreground hover:bg-white/10 hover:text-white"
                >
                  <X size={20} />
                </button>
              </div>

              <form onSubmit={handleEditSubmit} className="mt-4 space-y-4">
                {formError && (
                  <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-300">
                    {formError}
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Graph Title <span className="text-rose-400">*</span>
                  </label>
                  <Input
                    value={formTitle}
                    onChange={(e) => setFormTitle(e.target.value)}
                    placeholder="e.g. AI System Architecture"
                    className="mt-1.5"
                    maxLength={150}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Description
                  </label>
                  <textarea
                    value={formDescription}
                    onChange={(e) => setFormDescription(e.target.value)}
                    placeholder="Brief summary..."
                    rows={3}
                    maxLength={2000}
                    className="mt-1.5 w-full rounded-xl border border-white/10 bg-white/[0.04] p-3 text-sm text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                    Visibility
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <button
                      type="button"
                      onClick={() => setFormVisibility("PRIVATE")}
                      className={`flex flex-col items-start rounded-xl border p-3 text-left transition ${
                        formVisibility === "PRIVATE"
                          ? "border-cyan-400/50 bg-cyan-400/10 text-cyan-100"
                          : "border-white/10 bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
                      }`}
                    >
                      <div className="flex items-center gap-2 font-medium text-sm">
                        <Lock size={16} className={formVisibility === "PRIVATE" ? "text-cyan-400" : ""} />
                        Private
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground">Only accessible by owner and members.</p>
                    </button>

                    <button
                      type="button"
                      onClick={() => setFormVisibility("PUBLIC")}
                      className={`flex flex-col items-start rounded-xl border p-3 text-left transition ${
                        formVisibility === "PUBLIC"
                          ? "border-cyan-400/50 bg-cyan-400/10 text-cyan-100"
                          : "border-white/10 bg-white/[0.03] text-slate-400 hover:bg-white/[0.06]"
                      }`}
                    >
                      <div className="flex items-center gap-2 font-medium text-sm">
                        <Eye size={16} className={formVisibility === "PUBLIC" ? "text-cyan-400" : ""} />
                        Public
                      </div>
                      <p className="mt-1 text-xs text-muted-foreground">Visible to anyone on the platform.</p>
                    </button>
                  </div>
                </div>

                <div className="mt-6 flex justify-end gap-3 border-t border-white/10 pt-4">
                  <Button type="button" variant="ghost" onClick={() => setEditingGraph(null)}>
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    disabled={updateGraph.isPending}
                    className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-2"
                  >
                    {updateGraph.isPending && <Loader2 size={16} className="animate-spin" />}
                    Save Changes
                  </Button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* DELETE CONFIRMATION MODAL */}
      <AnimatePresence>
        {deletingGraph && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="w-full max-w-md overflow-hidden rounded-2xl border border-rose-500/20 bg-[rgba(16,10,18,0.96)] p-6 shadow-2xl backdrop-blur-2xl"
            >
              <div className="flex items-center gap-3 border-b border-white/10 pb-4 text-rose-400">
                <Trash2 size={24} />
                <h3 className="text-xl font-semibold text-slate-100">Delete Knowledge Graph</h3>
              </div>

              <div className="mt-4 space-y-2">
                <p className="text-sm text-slate-300">
                  Are you sure you want to delete <span className="font-semibold text-white">&quot;{deletingGraph.title}&quot;</span>?
                </p>
                <p className="text-xs text-muted-foreground">
                  This action will soft-delete the graph space. All associated nodes and edges will be hidden.
                </p>
              </div>

              <div className="mt-6 flex justify-end gap-3 border-t border-white/10 pt-4">
                <Button type="button" variant="ghost" onClick={() => setDeletingGraph(null)}>
                  Cancel
                </Button>
                <Button
                  type="button"
                  disabled={deleteGraph.isPending}
                  onClick={handleDeleteConfirm}
                  className="bg-rose-600 text-white hover:bg-rose-500 gap-2 font-semibold"
                >
                  {deleteGraph.isPending && <Loader2 size={16} className="animate-spin" />}
                  Delete Graph
                </Button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </section>
  );
}
