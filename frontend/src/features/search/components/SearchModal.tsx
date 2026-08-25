import { AnimatePresence, motion } from "framer-motion";
import {
  ArrowRight,
  Calendar,
  Clock,
  FileText,
  Filter,
  Folder,
  GitCommit,
  Hash,
  HelpCircle,
  History,
  Network,
  RotateCcw,
  Search,
  SlidersHorizontal,
  Sparkles,
  Terminal,
  Trash2,
  User,
  X,
  Zap
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAdvancedSearch } from "../hooks/useAdvancedSearch";
import { useGlobalSearch } from "../hooks/useGlobalSearch";
import type {
  DateRangeOption,
  SearchResultCategory,
  SearchResultItem,
  SortOption
} from "../types";
import { GraphPathPreview } from "./GraphPathPreview";

interface SearchModalProps {
  onSelectAction?: (item: SearchResultItem) => void;
}

const CATEGORY_TABS: { id: SearchResultCategory; label: string; icon: React.ElementType }[] = [
  { id: "all", label: "All", icon: Search },
  { id: "nodes", label: "Nodes", icon: FileText },
  { id: "edges", label: "Edges", icon: GitCommit },
  { id: "tags", label: "Tags", icon: Hash },
  { id: "people", label: "People", icon: User },
  { id: "workspaces", label: "Workspaces", icon: Folder },
  { id: "commands", label: "Commands", icon: Terminal }
];

export function SearchModal({ onSelectAction }: SearchModalProps) {
  const navigate = useNavigate();
  const { isSearchOpen, closeSearch } = useGlobalSearch();
  const {
    query,
    setQuery,
    cleanQuery,
    isCommandMode,
    isTagMode,
    filterState,
    setFilterState,
    recentSearches,
    suggestions,
    tagSuggestions,
    categoryCounts,
    selectedIndex,
    setSelectedIndex,
    addRecentSearch,
    removeRecentSearch,
    clearRecentSearches,
    selectTag
    ,isSearchLoading
    ,isSearchError
    ,searchError
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

  // Reset local state when modal closes
  useEffect(() => {
    if (!isSearchOpen) {
      setShowAdvancedFilters(false);
      setQuery("");
    }
  }, [isSearchOpen]);

  // Handle global shortcuts and modal keyboard navigation
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

  // Scroll active item into view smoothly
  useEffect(() => {
    if (!resultsContainerRef.current) return;
    const activeEl = resultsContainerRef.current.querySelector(`[data-index="${selectedIndex}"]`);
    if (activeEl) {
      activeEl.scrollIntoView({ block: "nearest", behavior: "smooth" });
    }
  }, [selectedIndex]);

  const handleSelectItem = (item: SearchResultItem) => {
    if (query.trim()) {
      addRecentSearch(query.trim(), item.category);
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

  const getItemIcon = (category: SearchResultCategory, type?: string) => {
    if (category === "commands") return Terminal;
    if (category === "people") return User;
    if (category === "workspaces") return Folder;
    if (category === "tags") return Hash;
    if (category === "edges") return GitCommit;
    if (type === "Decision") return Zap;
    if (type === "Document") return FileText;
    if (type === "Service") return Terminal;
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
          className="relative flex w-full max-w-3xl flex-col max-h-[82vh] overflow-hidden rounded-2xl border border-white/12 bg-[rgba(8,12,22,0.92)] shadow-2xl shadow-cyan-950/40 backdrop-blur-2xl"
          initial={{ opacity: 0, scale: 0.96, y: -12 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.96, y: -12 }}
          transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          {/* Top Header & Search Input Bar */}
          <div className="relative flex items-center gap-3 border-b border-white/10 px-4 py-3.5 bg-white/[0.02]">
            {isCommandMode ? (
              <Terminal size={19} className="text-cyan-400 animate-pulse" />
            ) : isTagMode ? (
              <Hash size={19} className="text-violet-400" />
            ) : (
              <Search size={19} className="text-cyan-400" />
            )}

            {/* Selected Tag Pill */}
            {filterState.selectedTag && (
              <span className="flex items-center gap-1 rounded-md border border-violet-500/40 bg-violet-950/60 px-2 py-1 text-xs font-semibold text-violet-300">
                #{filterState.selectedTag}
                <button
                  onClick={() => selectTag(filterState.selectedTag!)}
                  className="hover:text-white"
                >
                  <X size={12} />
                </button>
              </span>
            )}

            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search nodes, edges, tags, people or type '>' for commands..."
              className="h-10 flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none"
            />

            {query && (
              <button
                onClick={() => setQuery("")}
                className="rounded-md p-1 text-slate-400 hover:bg-white/10 hover:text-foreground"
                title="Clear input"
              >
                <X size={16} />
              </button>
            )}

            <button
              onClick={() => setShowAdvancedFilters((prev) => !prev)}
              className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs transition ${
                showAdvancedFilters || filterState.dateRange !== "any" || filterState.minConfidence > 0
                  ? "border-cyan-500/40 bg-cyan-950/40 text-cyan-300"
                  : "border-white/10 bg-white/[0.04] text-muted-foreground hover:bg-white/[0.08]"
              }`}
              title="Toggle filter controls"
            >
              <SlidersHorizontal size={13} />
              <span className="hidden sm:inline">Filters</span>
            </button>

            <span className="rounded-md border border-white/10 bg-black/40 px-1.5 py-0.5 font-mono text-[11px] text-slate-400">
              ESC
            </span>
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
                  {/* Date Range Filter */}
                  <div className="flex items-center gap-2">
                    <Calendar size={13} className="text-cyan-400" />
                    <span>Updated:</span>
                    {(["any", "today", "week", "month"] as DateRangeOption[]).map((range) => (
                      <button
                        key={range}
                        onClick={() =>
                          setFilterState((prev) => ({ ...prev, dateRange: range }))
                        }
                        className={`capitalize rounded px-2 py-0.5 text-[11px] transition ${
                          filterState.dateRange === range
                            ? "bg-cyan-500/20 text-cyan-300 font-semibold border border-cyan-500/40"
                            : "hover:bg-white/10 text-slate-400"
                        }`}
                      >
                        {range}
                      </button>
                    ))}
                  </div>

                  {/* Confidence Filter */}
                  <div className="flex items-center gap-2">
                    <Sparkles size={13} className="text-violet-400" />
                    <span>Confidence:</span>
                    {[
                      { label: "All", val: 0 },
                      { label: ">80%", val: 80 },
                      { label: ">90%", val: 90 }
                    ].map((conf) => (
                      <button
                        key={conf.label}
                        onClick={() =>
                          setFilterState((prev) => ({ ...prev, minConfidence: conf.val }))
                        }
                        className={`rounded px-2 py-0.5 text-[11px] transition ${
                          filterState.minConfidence === conf.val
                            ? "bg-violet-500/20 text-violet-300 font-semibold border border-violet-500/40"
                            : "hover:bg-white/10 text-slate-400"
                        }`}
                      >
                        {conf.label}
                      </button>
                    ))}
                  </div>

                  {/* Sort Filter */}
                  <div className="flex items-center gap-2 ml-auto">
                    <Filter size={13} className="text-emerald-400" />
                    <span>Sort:</span>
                    <select
                      value={filterState.sortBy}
                      onChange={(e) =>
                        setFilterState((prev) => ({
                          ...prev,
                          sortBy: e.target.value as SortOption
                        }))
                      }
                      className="rounded border border-white/10 bg-slate-900 px-2 py-0.5 text-[11px] text-slate-200 outline-none"
                    >
                      <option value="relevance">Relevance</option>
                      <option value="recent">Recently Updated</option>
                      <option value="confidence">Highest Confidence</option>
                      <option value="connections">Most Connected</option>
                    </select>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Category Filter Tabs with sliding active layout */}
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
                        isActive
                          ? "bg-cyan-400/20 text-cyan-200"
                          : "bg-white/10 text-slate-400"
                      }`}
                    >
                      {count}
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          {/* Main Results or Empty/Recent View Area */}
          <div ref={resultsContainerRef} className="flex-1 overflow-y-auto p-3 space-y-4">
            {/* View 1: Blank Query - Show Recent Searches and Popular Tag Search */}
            {!query && !filterState.selectedTag && filterState.category === "all" ? (
              <div className="space-y-4 p-1">
                {/* Recent Searches */}
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

                {/* Popular Tag Search */}
                <div>
                  <span className="flex items-center gap-1.5 px-2 pb-2 text-xs font-semibold uppercase tracking-wider text-slate-400">
                    <Hash size={13} className="text-violet-400" /> Popular Knowledge Tags
                  </span>
                  <div className="flex flex-wrap gap-1.5 px-2">
                    {tagSuggestions.map((tag) => {
                      const isSelected = filterState.selectedTag === tag.name;
                      return (
                        <button
                          key={tag.name}
                          onClick={() => selectTag(tag.name)}
                          className={`flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs transition ${
                            isSelected
                              ? "border-violet-500/60 bg-violet-950/60 text-violet-200 shadow-glow"
                              : "border-white/10 bg-white/[0.03] text-slate-300 hover:border-violet-500/40 hover:bg-white/[0.08]"
                          }`}
                        >
                          <Hash size={12} className="text-violet-400" />
                          <span>{tag.name}</span>
                          <span className="rounded bg-white/10 px-1 py-0.2 text-[10px] text-slate-400">
                            {tag.count}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Quick Hint Card */}
                <div className="rounded-xl border border-white/8 bg-gradient-to-r from-violet-950/20 via-cyan-950/20 to-slate-900/40 p-3 text-xs text-slate-400">
                  <div className="flex items-center gap-2 font-medium text-slate-200 pb-1">
                    <Zap size={14} className="text-cyan-400" /> Pro Search Tips
                  </div>
                  <ul className="space-y-1 list-disc list-inside text-[11px] text-slate-400">
                    <li>Type <code className="text-cyan-300 font-mono">&gt;</code> to jump into Command Mode (e.g. <code className="text-slate-300">&gt; create node</code>)</li>
                    <li>Type <code className="text-violet-300 font-mono">#</code> to search knowledge graph tags directly</li>
                    <li>Use <kbd className="bg-white/10 px-1 rounded text-slate-300">Tab</kbd> to cycle categories or <kbd className="bg-white/10 px-1 rounded text-slate-300">↑↓</kbd> to navigate results</li>
                  </ul>
                </div>
              </div>
            ) : isSearchLoading ? (
              <div className="flex items-center justify-center py-12 text-sm text-cyan-300">
                <Search size={18} className="mr-2 animate-pulse" /> Searching...
              </div>
            ) : isSearchError ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <HelpCircle size={22} className="mb-3 text-rose-300" />
                <h4 className="text-sm font-semibold text-slate-200">Search failed</h4>
                <p className="mt-1 max-w-sm text-xs text-slate-400">{(searchError as Error)?.message ?? "Unable to search the workspace."}</p>
              </div>
            ) : suggestions.length === 0 ? (
              /* View 2: No Search Results Found */
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-white/10 bg-white/[0.04] text-slate-400 mb-3">
                  <HelpCircle size={22} />
                </div>
                <h4 className="text-sm font-semibold text-slate-200">No matching search results</h4>
                <p className="mt-1 text-xs text-slate-400 max-w-sm">
                  We couldn't find any graph entities or commands matching "{cleanQuery}". Try adjusting your filters or search keywords.
                </p>
                <div className="mt-4 flex gap-2">
                  <button
                    onClick={() => {
                      setQuery("");
                      setFilterState({
                        category: "all",
                        dateRange: "any",
                        minConfidence: 0,
                        sortBy: "relevance"
                      });
                    }}
                    className="flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/[0.05] px-3 py-1.5 text-xs text-slate-300 hover:bg-white/10"
                  >
                    <RotateCcw size={13} /> Reset search filters
                  </button>
                </div>
              </div>
            ) : (
              /* View 3: Suggestions & Search Results List */
              <div className="space-y-1.5">
                {suggestions.map((item, idx) => {
                  const isSelected = idx === selectedIndex;
                  const ItemIcon = getItemIcon(item.category, item.type);

                  return (
                    <div
                      key={item.id}
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
                          <div className="flex items-center gap-2">
                            <span className="font-medium text-sm text-slate-100 truncate">
                              {item.title}
                            </span>

                            {item.type && (
                              <span className="rounded border border-white/10 bg-white/5 px-1.5 py-0.2 text-[10px] font-mono text-slate-400">
                                {item.type}
                              </span>
                            )}

                            {item.shortcut && (
                              <span className="ml-auto rounded border border-cyan-500/30 bg-cyan-950/50 px-1.5 py-0.5 font-mono text-[10px] text-cyan-300">
                                {item.shortcut}
                              </span>
                            )}

                            {item.confidence !== undefined && (
                              <span className="ml-auto hidden sm:inline-block rounded border border-emerald-500/30 bg-emerald-950/40 px-1.5 py-0.2 text-[10px] font-semibold text-emerald-300">
                                {item.confidence}% confidence
                              </span>
                            )}
                          </div>

                          {item.subtitle && (
                            <p className="mt-0.5 text-xs text-slate-400 truncate">
                              {item.subtitle}
                            </p>
                          )}

                          {item.description && (
                            <p className="mt-1 text-xs text-slate-400 line-clamp-2 leading-relaxed">
                              {item.description}
                            </p>
                          )}

                          {/* Graph Path Preview for Edges */}
                          {item.category === "edges" && (
                            <GraphPathPreview
                              sourceNode={item.sourceNode}
                              targetNode={item.targetNode}
                              relationshipType={item.type}
                              confidence={item.confidence}
                            />
                          )}

                          {/* Tags row */}
                          {item.tags && item.tags.length > 0 && (
                            <div className="mt-2 flex flex-wrap items-center gap-1">
                              {item.tags.map((t) => (
                                <button
                                  key={t}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    selectTag(t);
                                  }}
                                  className="inline-flex items-center gap-0.5 rounded bg-violet-950/40 border border-violet-500/20 px-1.5 py-0.2 text-[10px] font-medium text-violet-300 hover:border-violet-500/50"
                                >
                                  <Hash size={10} />
                                  {t}
                                </button>
                              ))}
                            </div>
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

          {/* Footer keyboard hints bar */}
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
              <span className="flex items-center gap-1 hidden sm:inline-flex">
                <kbd className="rounded border border-white/10 bg-white/10 px-1 font-mono text-[10px]">Tab</kbd>
                Filter Category
              </span>
            </div>

            <div className="flex items-center gap-2 text-slate-400">
              <span className="hidden sm:inline">Knowledge Network Advanced Search</span>
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

