import { AnimatePresence, motion } from "framer-motion";
import {
  ArrowRight,
  Clock,
  FileText,
  Filter,
  Folder,
  Globe,
  HelpCircle,
  History,
  Lock,
  Network,
  RotateCcw,
  Search,
  SlidersHorizontal,
  Terminal,
  Trash2,
  X,
  Zap
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAdvancedSearch } from "../hooks/useAdvancedSearch";
import { useGlobalSearch } from "../hooks/useGlobalSearch";
import type {
  NodeTypeFilter,
  SearchResultCategory,
  SearchResultItem,
  VisibilityFilter
} from "../types";

interface SearchModalProps {
  onSelectAction?: (item: SearchResultItem) => void;
}

const CATEGORY_TABS: { id: SearchResultCategory; label: string; icon: React.ElementType }[] = [
  { id: "all", label: "All", icon: Search },
  { id: "graphs", label: "Graphs", icon: Folder },
  { id: "nodes", label: "Nodes", icon: FileText },
  { id: "commands", label: "Commands", icon: Terminal }
];

const NODE_TYPES: NodeTypeFilter[] = ["all", "Concept", "Document", "Person", "Decision", "Service", "Feature"];

export function SearchModal({ onSelectAction }: SearchModalProps) {
  const navigate = useNavigate();
  const { isSearchOpen, closeSearch } = useGlobalSearch();
  const {
    query,
    setQuery,
    cleanQuery,
    isCommandMode,
    filterState,
    setFilterState,
    recentSearches,
    suggestions,
    categoryCounts,
    selectedIndex,
    setSelectedIndex,
    addRecentSearch,
    removeRecentSearch,
    clearRecentSearches,
    isSearchLoading,
    isSearchError,
    searchError,
    refetchSearch
  } = useAdvancedSearch();

  const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const resultsContainerRef = useRef<HTMLDivElement>(null);

  // Auto focus input when opened
  useEffect(() => {
    if (isSearchOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [isSearchOpen]);

  // Reset query on close
  useEffect(() => {
    if (!isSearchOpen) {
      setShowAdvancedFilters(false);
    }
  }, [isSearchOpen]);

  // Keyboard navigation
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (!isSearchOpen) return;

      if (e.key === "Escape") {
        e.preventDefault();
        closeSearch();
        return;
      }

      if (e.key === "ArrowDown") {
        e.preventDefault();
        setSelectedIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : 0));
        return;
      }

      if (e.key === "ArrowUp") {
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : suggestions.length - 1));
        return;
      }

      if (e.key === "Enter" && suggestions.length > 0) {
        e.preventDefault();
        const selected = suggestions[selectedIndex];
        if (selected) {
          handleSelectItem(selected);
        }
        return;
      }

      if (e.key === "Tab") {
        e.preventDefault();
        const categories: SearchResultCategory[] = CATEGORY_TABS.map((t) => t.id);
        const currentIndex = categories.indexOf(filterState.category);
        const nextIndex = e.shiftKey
          ? (currentIndex - 1 + categories.length) % categories.length
          : (currentIndex + 1) % categories.length;

        setFilterState((prev) => ({ ...prev, category: categories[nextIndex]! }));
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isSearchOpen, suggestions, selectedIndex, filterState.category, closeSearch]);

  // Scroll active item into view
  useEffect(() => {
    if (!resultsContainerRef.current) return;
    const activeEl = resultsContainerRef.current.querySelector(`[data-index="${selectedIndex}"]`);
    if (activeEl) {
      activeEl.scrollIntoView({ block: "nearest", behavior: "smooth" });
    }
  }, [selectedIndex]);

  const handleSelectItem = (item: SearchResultItem) => {
    if (query.trim()) {
      addRecentSearch(query.trim());
    }

    if (onSelectAction) {
      onSelectAction(item);
    }

    if (item.actionId === "TOGGLE_THEME") {
      document.documentElement.classList.toggle("dark");
    } else if (item.url) {
      navigate(item.url);
    }

    closeSearch();
  };

  const getItemIcon = (resultType: string, nodeTypeName?: string) => {
    if (resultType === "COMMAND") return Terminal;
    if (resultType === "GRAPH") return Folder;
    if (nodeTypeName === "Decision") return Zap;
    if (nodeTypeName === "Document") return FileText;
    if (nodeTypeName === "Service") return Terminal;
    return Network;
  };

  if (!isSearchOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        className="fixed inset-0 z-[100] flex items-start justify-center bg-black/70 px-3 pt-[8vh] sm:pt-[12vh] backdrop-blur-xl"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onMouseDown={closeSearch}
      >
        <motion.div
          className="relative flex w-full max-w-3xl flex-col max-h-[82vh] overflow-hidden rounded-2xl border border-white/12 bg-[rgba(8,12,22,0.95)] shadow-2xl shadow-cyan-950/40 backdrop-blur-2xl"
          initial={{ opacity: 0, scale: 0.96, y: -12 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.96, y: -12 }}
          transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          {/* Top Header & Search Input Bar */}
          <div className="relative flex items-center gap-2.5 border-b border-white/10 px-3.5 py-3 bg-white/[0.02]">
            {isCommandMode ? (
              <Terminal size={19} className="text-cyan-400 animate-pulse shrink-0" />
            ) : (
              <Search size={19} className="text-cyan-400 shrink-0" />
            )}

            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search your graphs and knowledge..."
              className="h-10 flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none min-w-0"
              aria-label="Search query input"
            />

            {query && (
              <button
                onClick={() => setQuery("")}
                className="rounded-md p-1.5 text-slate-400 hover:bg-white/10 hover:text-foreground transition shrink-0"
                title="Clear input"
                aria-label="Clear search input"
              >
                <X size={16} />
              </button>
            )}

            <button
              onClick={() => setShowAdvancedFilters((prev) => !prev)}
              className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs transition shrink-0 ${
                showAdvancedFilters || filterState.nodeType !== "all" || filterState.visibility !== "all"
                  ? "border-cyan-500/40 bg-cyan-950/40 text-cyan-300"
                  : "border-white/10 bg-white/[0.04] text-muted-foreground hover:bg-white/[0.08]"
              }`}
              title="Toggle filter controls"
              aria-label="Toggle search filters"
            >
              <SlidersHorizontal size={13} />
              <span className="hidden sm:inline">Filters</span>
            </button>

            {/* Prominent Close Button */}
            <button
              onClick={closeSearch}
              aria-label="Close search"
              title="Close search (Esc)"
              className="flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.06] hover:bg-rose-500/20 hover:border-rose-500/40 hover:text-rose-200 px-2.5 py-1.5 text-xs font-medium text-slate-300 transition shrink-0 focus:outline-none focus:ring-2 focus:ring-cyan-500/50"
            >
              <X size={16} className="text-slate-300 group-hover:text-rose-200" />
              <span className="hidden sm:inline">Close</span>
              <kbd className="hidden md:inline rounded border border-white/10 bg-black/40 px-1 py-0.2 font-mono text-[10px] text-slate-400">
                Esc
              </kbd>
            </button>
          </div>

          {/* Expandable Advanced Filters Drawer */}
          <AnimatePresence>
            {showAdvancedFilters && (
              <motion.div
                initial={{ height: 0, opacity: 0 }}
                animate={{ height: "auto", opacity: 1 }}
                exit={{ height: 0, opacity: 0 }}
                className="overflow-hidden border-b border-white/10 bg-black/40 px-4 py-3 text-xs text-slate-300"
              >
                <div className="flex flex-wrap items-center gap-4">
                  {/* Node Type Filter */}
                  <div className="flex items-center gap-2">
                    <Filter size={13} className="text-cyan-400" />
                    <span>Node Type:</span>
                    <select
                      value={filterState.nodeType}
                      onChange={(e) =>
                        setFilterState((prev) => ({
                          ...prev,
                          nodeType: e.target.value as NodeTypeFilter
                        }))
                      }
                      className="rounded border border-white/10 bg-slate-900 px-2 py-1 text-[11px] text-slate-200 outline-none"
                    >
                      {NODE_TYPES.map((t) => (
                        <option key={t} value={t}>
                          {t === "all" ? "All Types" : t}
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Visibility Filter */}
                  <div className="flex items-center gap-2">
                    <Globe size={13} className="text-violet-400" />
                    <span>Visibility:</span>
                    {(["all", "PUBLIC", "PRIVATE"] as VisibilityFilter[]).map((v) => (
                      <button
                        key={v}
                        onClick={() =>
                          setFilterState((prev) => ({ ...prev, visibility: v }))
                        }
                        className={`capitalize rounded px-2 py-0.5 text-[11px] transition ${
                          filterState.visibility === v
                            ? "bg-violet-500/20 text-violet-300 font-semibold border border-violet-500/40"
                            : "hover:bg-white/10 text-slate-400"
                        }`}
                      >
                        {v}
                      </button>
                    ))}
                  </div>

                  {/* Reset Filters */}
                  {(filterState.nodeType !== "all" || filterState.visibility !== "all") && (
                    <button
                      onClick={() =>
                        setFilterState((prev) => ({
                          ...prev,
                          nodeType: "all",
                          visibility: "all"
                        }))
                      }
                      className="ml-auto flex items-center gap-1 text-[11px] text-cyan-400 hover:underline"
                    >
                      <RotateCcw size={11} /> Clear filters
                    </button>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Category Filter Tabs */}
          <div className="flex items-center gap-1 overflow-x-auto border-b border-white/10 bg-black/20 px-3 py-2 scrollbar-none">
            {CATEGORY_TABS.map((tab) => {
              const Icon = tab.icon;
              const isActive = filterState.category === tab.id;
              const count = categoryCounts[tab.id];

              return (
                <button
                  key={tab.id}
                  onClick={() => setFilterState((prev) => ({ ...prev, category: tab.id }))}
                  className={`relative flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition ${
                    isActive ? "text-cyan-300" : "text-slate-400 hover:text-slate-200 hover:bg-white/[0.04]"
                  }`}
                >
                  {isActive && (
                    <motion.div
                      layoutId="activeSearchCategoryTab"
                      className="absolute inset-0 rounded-lg border border-cyan-500/30 bg-cyan-500/10"
                      transition={{ type: "spring", stiffness: 400, damping: 30 }}
                    />
                  )}
                  <Icon size={13} className="relative z-10" />
                  <span className="relative z-10">{tab.label}</span>
                  {count > 0 && (
                    <span
                      className={`relative z-10 rounded-full px-1.5 py-0.2 text-[10px] ${
                        isActive ? "bg-cyan-400/20 text-cyan-200" : "bg-white/10 text-slate-400"
                      }`}
                    >
                      {count}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Main Results Container */}
          <div ref={resultsContainerRef} className="flex-1 overflow-y-auto p-3 space-y-4 min-h-[220px]">
            {/* Inviting Empty Search State */}
            {!query && filterState.category === "all" ? (
              <div className="space-y-4 p-1">
                {/* Friendly Header Banner */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/8 pb-3 px-1">
                  <div>
                    <h3 className="text-sm font-semibold text-slate-200">Search your graphs and knowledge</h3>
                    <p className="text-xs text-slate-400 mt-0.5">Start typing to find something, or click Close to return to your workspace.</p>
                  </div>
                  <button
                    onClick={closeSearch}
                    className="self-start sm:self-auto text-xs font-medium text-cyan-400 hover:text-cyan-300 transition flex items-center gap-1 shrink-0"
                    title="Close search modal"
                  >
                    Close search <X size={13} />
                  </button>
                </div>

                {recentSearches.length > 0 && (
                  <div>
                    <div className="flex items-center justify-between px-2 pb-2">
                      <span className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-slate-400">
                        <History size={13} className="text-cyan-400" /> Recent Searches
                      </span>
                      <button
                        onClick={clearRecentSearches}
                        className="flex items-center gap-1 text-[11px] text-slate-500 hover:text-rose-400 transition"
                      >
                        <Trash2 size={11} /> Clear history
                      </button>
                    </div>

                    <div className="grid grid-cols-1 gap-1.5 sm:grid-cols-2">
                      {recentSearches.map((item) => (
                        <div
                          key={item.id}
                          onClick={() => setQuery(item.query)}
                          className="group flex items-center justify-between rounded-lg border border-white/8 bg-white/[0.03] px-3 py-2 text-xs text-slate-300 hover:border-cyan-500/30 hover:bg-white/[0.07] hover:text-cyan-200 transition cursor-pointer"
                        >
                          <div className="flex items-center gap-2 truncate">
                            <Clock size={13} className="text-slate-500 group-hover:text-cyan-400" />
                            <span className="truncate">{item.query}</span>
                          </div>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              removeRecentSearch(item.id);
                            }}
                            className="text-slate-500 opacity-0 group-hover:opacity-100 hover:text-rose-400 transition p-0.5"
                            title="Remove item"
                          >
                            <X size={12} />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Quick Hint Card */}
                <div className="rounded-xl border border-white/8 bg-gradient-to-r from-violet-950/20 via-cyan-950/20 to-slate-900/40 p-3 text-xs text-slate-400">
                  <div className="flex items-center gap-2 font-medium text-slate-200 pb-1">
                    <Zap size={14} className="text-cyan-400" /> Quick Search Tips
                  </div>
                  <ul className="space-y-1 list-disc list-inside text-[11px] text-slate-400">
                    <li>Search for graphs by title or description (e.g. <code className="text-cyan-300 font-mono">Spring</code>)</li>
                    <li>Search for knowledge nodes directly across all authorized workspaces</li>
                    <li>Type <code className="text-cyan-300 font-mono">&gt;</code> to jump into Command Mode</li>
                  </ul>
                </div>
              </div>
            ) : isSearchLoading ? (
              /* Loading State */
              <div className="flex items-center justify-center py-16 text-sm text-cyan-300">
                <Search size={18} className="mr-2 animate-pulse" /> Searching knowledge network...
              </div>
            ) : isSearchError ? (
              /* Error State with Retry */
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <HelpCircle size={24} className="mb-3 text-rose-400" />
                <h4 className="text-sm font-semibold text-slate-200">Search Failed</h4>
                <p className="mt-1 max-w-sm text-xs text-slate-400">
                  {(searchError as Error)?.message || "Could not retrieve search results from server."}
                </p>
                <button
                  onClick={() => refetchSearch()}
                  className="mt-4 flex items-center gap-1.5 rounded-lg border border-cyan-500/40 bg-cyan-950/40 px-3 py-1.5 text-xs text-cyan-300 hover:bg-cyan-900/40 transition"
                >
                  <RotateCcw size={13} /> Retry Search
                </button>
              </div>
            ) : suggestions.length === 0 ? (
              /* Empty State */
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-white/10 bg-white/[0.04] text-slate-400 mb-3">
                  <HelpCircle size={22} />
                </div>
                <h4 className="text-sm font-semibold text-slate-200">No results found</h4>
                <p className="mt-1 text-xs text-slate-400 max-w-sm">
                  We couldn't find any graph entities matching "{cleanQuery}". Try adjusting your filters or keywords.
                </p>
                <div className="mt-4 flex gap-2">
                  <button
                    onClick={() => {
                      setQuery("");
                      setFilterState({ category: "all", nodeType: "all", visibility: "all" });
                    }}
                    className="flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.05] px-3 py-1.5 text-xs text-slate-300 hover:bg-white/10 transition"
                  >
                    <RotateCcw size={13} /> Reset search
                  </button>
                </div>
              </div>
            ) : (
              /* Results List */
              <div className="space-y-1.5">
                {suggestions.map((item: SearchResultItem, idx: number) => {
                  const isSelected = idx === selectedIndex;
                  const ItemIcon = getItemIcon(item.resultType, item.nodeTypeName);

                  return (
                    <div
                      key={`${item.resultType}-${item.id}`}
                      data-index={idx}
                      onClick={() => handleSelectItem(item)}
                      onMouseEnter={() => setSelectedIndex(idx)}
                      className={`group relative flex flex-col rounded-xl border p-3 transition cursor-pointer ${
                        isSelected
                          ? "border-cyan-500/40 bg-cyan-500/[0.08] text-foreground shadow-glow"
                          : "border-white/8 bg-white/[0.025] hover:border-white/16 hover:bg-white/[0.05]"
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div
                          className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg border text-sm transition ${
                            isSelected
                              ? "border-cyan-400/50 bg-cyan-950/80 text-cyan-300"
                              : "border-white/10 bg-white/[0.04] text-slate-400 group-hover:text-slate-200"
                          }`}
                        >
                          <ItemIcon size={17} />
                        </div>

                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-semibold text-sm text-slate-100 truncate">
                              {item.title}
                            </span>

                            {/* Result Type Badge */}
                            <span
                              className={`rounded border px-1.5 py-0.2 text-[10px] font-mono uppercase tracking-wider ${
                                item.resultType === "GRAPH"
                                  ? "border-cyan-500/40 bg-cyan-950/60 text-cyan-300"
                                  : item.resultType === "NODE"
                                  ? "border-violet-500/40 bg-violet-950/60 text-violet-300"
                                  : "border-slate-500/40 bg-slate-900 text-slate-300"
                              }`}
                            >
                              {item.resultType}
                            </span>

                            {/* Node Type Badge */}
                            {item.nodeTypeName && (
                              <span
                                className="rounded border border-white/10 px-1.5 py-0.2 text-[10px] font-medium"
                                style={{
                                  backgroundColor: item.nodeTypeColor ? `${item.nodeTypeColor}20` : undefined,
                                  borderColor: item.nodeTypeColor ? `${item.nodeTypeColor}50` : undefined,
                                  color: item.nodeTypeColor || "#CBD5E1"
                                }}
                              >
                                {item.nodeTypeName}
                              </span>
                            )}

                            {/* Parent Graph Badge */}
                            {item.resultType === "NODE" && item.graphTitle && (
                              <span className="flex items-center gap-1 text-[11px] text-slate-400 ml-auto">
                                <Folder size={11} className="text-cyan-400" />
                                <span className="truncate max-w-[150px]">{item.graphTitle}</span>
                              </span>
                            )}

                            {/* Visibility / Graph Node Count */}
                            {item.resultType === "GRAPH" && (
                              <div className="ml-auto flex items-center gap-2 text-[11px] text-slate-400">
                                {item.visibility === "PUBLIC" ? (
                                  <span className="flex items-center gap-0.5 text-emerald-400">
                                    <Globe size={11} /> Public
                                  </span>
                                ) : (
                                  <span className="flex items-center gap-0.5 text-slate-400">
                                    <Lock size={11} /> Private
                                  </span>
                                )}
                                <span>• {item.nodeCount ?? 0} nodes</span>
                                <span>• {item.edgeCount ?? 0} edges</span>
                              </div>
                            )}
                          </div>

                          {item.description && (
                            <p className="mt-1 text-xs text-slate-400 line-clamp-2 leading-relaxed">
                              {item.description}
                            </p>
                          )}
                        </div>

                        <div className="shrink-0 flex items-center gap-1 self-center pl-2">
                          <ArrowRight
                            size={16}
                            className={`transition ${
                              isSelected
                                ? "text-cyan-300 translate-x-0.5 opacity-100"
                                : "text-slate-600 opacity-0 group-hover:opacity-100"
                            }`}
                          />
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Footer keyboard hints */}
          <div className="flex flex-wrap items-center justify-between border-t border-white/10 bg-black/40 px-4 py-2 text-[11px] text-slate-400">
            <div className="flex items-center gap-3">
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-white/10 bg-white/10 px-1 font-mono text-[10px]">↑↓</kbd>
                Navigate
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-white/10 bg-white/10 px-1 font-mono text-[10px]">↵</kbd>
                Select
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border border-white/10 bg-white/10 px-1 font-mono text-[10px]">Esc</kbd>
                or Click <X size={11} className="inline text-slate-300" /> Close
              </span>
            </div>

            <div className="flex items-center gap-2 text-slate-400">
              <span className="hidden sm:inline">Knowledge Network Unified Search</span>
              <span className="rounded border border-cyan-500/30 bg-cyan-950/40 px-1.5 py-0.2 font-mono text-[10px] text-cyan-300">
                v2.0
              </span>
            </div>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
