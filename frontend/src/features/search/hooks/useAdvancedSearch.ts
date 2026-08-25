import { useCallback, useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { initialRecentSearches, popularTags } from "../data/searchData";
import { searchApi } from "../../../services";
import { useCurrentWorkspace } from "../../../hooks/use-queries";
import type {
  RecentSearchItem,
  SearchFilterState,
  SearchResultCategory,
  SearchResultItem
} from "../types";

const LOCAL_STORAGE_KEY = "kn_recent_searches_v1";

export function useAdvancedSearch() {
  const { workspaceId } = useCurrentWorkspace();
  const [query, setQuery] = useState("");
  const [filterState, setFilterState] = useState<SearchFilterState>({
    category: "all",
    dateRange: "any",
    minConfidence: 0,
    sortBy: "relevance"
  });

  const [recentSearches, setRecentSearches] = useState<RecentSearchItem[]>(() => {
    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch (e) {
      console.warn("Failed to read recent searches from localStorage", e);
    }
    return initialRecentSearches;
  });

  const [selectedIndex, setSelectedIndex] = useState(0);

  useEffect(() => {
    try {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(recentSearches));
    } catch (e) {
      console.warn("Failed to persist recent searches", e);
    }
  }, [recentSearches]);

  const isCommandMode = query.trim().startsWith(">") || filterState.category === "commands";
  const cleanQuery = useMemo(() => {
    if (query.trim().startsWith(">")) return query.trim().slice(1).trim();
    if (query.trim().startsWith("#")) return query.trim().slice(1).trim();
    return query.trim();
  }, [query]);

  const isTagMode = query.trim().startsWith("#") || Boolean(filterState.selectedTag);

  const { data: apiResults, isLoading: isSearchLoading, isError: isSearchError, error: searchError } = useQuery({
    queryKey: ["search-results", workspaceId, query, filterState],
    queryFn: () =>
      searchApi
        .search(
          workspaceId!,
          cleanQuery,
          filterState.selectedTag ? [filterState.selectedTag] : undefined,
          undefined,
          0,
          20,
          {
            updatedAfter: filterState.dateRange === "any" ? undefined : new Date(Date.now() - (
              filterState.dateRange === "today" ? 24 * 60 * 60 * 1000 :
              filterState.dateRange === "week" ? 7 * 24 * 60 * 60 * 1000 :
              30 * 24 * 60 * 60 * 1000
            )).toISOString(),
            minConfidence: filterState.minConfidence || undefined,
            sortBy: filterState.sortBy
          }
        )
        .then((r) => r.data),
    enabled: Boolean(workspaceId) && cleanQuery.length >= 2 && !isCommandMode && !isTagMode,
    staleTime: 5_000
  });

  const apiSuggestions = useMemo(() => {
    if (!apiResults?.content) return [];
    return apiResults.content.map((item: any) => ({
      id: item.id,
      category: item.type === "node" ? "nodes" : item.type === "edge" ? "edges" : item.category ?? "all",
      title: item.label,
      subtitle: item.nodeTypeName,
      description: typeof item.attributes?.description === "string" ? item.attributes.description : undefined,
      tags: item.tags,
      type: item.nodeTypeName,
      confidence: typeof item.attributes?.confidence === "number" ? item.attributes.confidence : undefined,
      connections: typeof item.attributes?.connections === "number" ? item.attributes.connections : 0,
      updatedAt: item.updatedAt,
      url: workspaceId ? `/graph?workspaceId=${workspaceId}` : undefined,
      shortcut: undefined,
      actionId: undefined
    })) as SearchResultItem[];
  }, [apiResults, workspaceId]);

  const suggestions = useMemo(() => {
    if (isCommandMode || isTagMode || cleanQuery.length < 2) {
      return apiSuggestions;
    }
    return apiSuggestions;
  }, [apiSuggestions, isCommandMode, isTagMode, cleanQuery]);

  useEffect(() => {
    setSelectedIndex(0);
  }, [suggestions.length, query, filterState]);

  const tagSuggestions = useMemo(() => {
    if (!query.trim().startsWith("#")) return popularTags;
    const tagQuery = query.trim().slice(1).toLowerCase();
    return popularTags.filter((t) => t.name.toLowerCase().includes(tagQuery));
  }, [query]);

  const addRecentSearch = useCallback(
    (q: string, category: SearchResultCategory = "all") => {
      const trimmed = q.trim();
      if (!trimmed || trimmed.length < 2 || trimmed.startsWith(">")) return;

      setRecentSearches((prev) => {
        const filtered = prev.filter((item) => item.query.toLowerCase() !== trimmed.toLowerCase());
        const newItem: RecentSearchItem = {
          id: `rec-${Date.now()}`,
          query: trimmed,
          timestamp: Date.now(),
          category
        };
        return [newItem, ...filtered].slice(0, 10);
      });
    },
    []
  );

  const removeRecentSearch = useCallback((id: string) => {
    setRecentSearches((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const clearRecentSearches = useCallback(() => {
    setRecentSearches([]);
  }, []);

  const selectTag = useCallback((tagName: string) => {
    setFilterState((prev) => ({
      ...prev,
      selectedTag: prev.selectedTag === tagName ? undefined : tagName
    }));
  }, []);

  const categoryCounts = useMemo(() => {
    const counts: Record<SearchResultCategory, number> = {
      all: apiResults?.totalElements ?? 0,
      nodes: apiResults?.content?.filter((r: any) => r.type === "node" || r.category === "nodes").length ?? 0,
      edges: apiResults?.content?.filter((r: any) => r.type === "edge" || r.category === "edges").length ?? 0,
      tags: popularTags.length,
      people: 0,
      workspaces: 0,
      commands: 0
    };
    return counts;
  }, [apiResults]);

  return {
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
    selectTag,
    isSearchLoading,
    isSearchError,
    searchError
  };
}