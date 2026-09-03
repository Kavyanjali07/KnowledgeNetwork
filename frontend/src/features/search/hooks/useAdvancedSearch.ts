import { useCallback, useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { initialRecentSearches } from "../data/searchData";
import { searchApi } from "../../../services";
import type { SearchItemResponse } from "../../../services";
import type {
  NodeTypeFilter,
  RecentSearchItem,
  SearchFilterState,
  SearchResultCategory,
  SearchResultItem,
  VisibilityFilter
} from "../types";

const LOCAL_STORAGE_KEY = "kn_recent_searches_v1";

const COMMAND_ITEMS: SearchResultItem[] = [
  {
    id: "cmd-create-graph",
    resultType: "COMMAND",
    title: "Create Graph",
    description: "Start a new knowledge workspace graph",
    actionId: "CREATE_GRAPH",
    url: "/graphs"
  },
  {
    id: "cmd-toggle-theme",
    resultType: "COMMAND",
    title: "Toggle Theme",
    description: "Switch between dark and light mode",
    actionId: "TOGGLE_THEME"
  },
  {
    id: "cmd-view-notifications",
    resultType: "COMMAND",
    title: "View Notifications",
    description: "Check your recent system and activity alerts",
    actionId: "VIEW_NOTIFICATIONS",
    url: "/notifications"
  }
];

export function useAdvancedSearch() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [query, setQuery] = useState(() => searchParams.get("q") || "");
  const [debouncedQuery, setDebouncedQuery] = useState(query);

  const [filterState, setFilterState] = useState<SearchFilterState>({
    category: (searchParams.get("type") as SearchResultCategory) || "all",
    nodeType: (searchParams.get("nodeType") as NodeTypeFilter) || "all",
    visibility: (searchParams.get("visibility") as VisibilityFilter) || "all"
  });

  const [recentSearches, setRecentSearches] = useState<RecentSearchItem[]>(() => {
    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.warn("Failed to read recent searches from localStorage", e);
    }
    return initialRecentSearches;
  });

  const [selectedIndex, setSelectedIndex] = useState(0);

  // Sync debounced query (300ms)
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(query.trim());
    }, 300);
    return () => clearTimeout(handler);
  }, [query]);

  // Persist recent searches
  useEffect(() => {
    try {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(recentSearches));
    } catch (e) {
      console.warn("Failed to persist recent searches", e);
    }
  }, [recentSearches]);

  // Command mode check
  const isCommandMode = query.trim().startsWith(">");
  const cleanQuery = useMemo(() => {
    if (query.trim().startsWith(">")) return query.trim().slice(1).trim();
    return query.trim();
  }, [query]);

  // Fetch unified search results from backend API
  const {
    data: apiResults,
    isLoading: isSearchLoading,
    isError: isSearchError,
    error: searchError,
    refetch
  } = useQuery({
    queryKey: ["unified-search", debouncedQuery, filterState.category, filterState.nodeType, filterState.visibility],
    queryFn: () =>
      searchApi
        .unifiedSearch({
          q: debouncedQuery,
          type: filterState.category === "commands" ? "all" : filterState.category,
          nodeType: filterState.nodeType === "all" ? undefined : filterState.nodeType,
          page: 0,
          size: 30
        })
        .then((r) => r.data),
    enabled: debouncedQuery.length >= 1 && !isCommandMode,
    staleTime: 5_000
  });

  // Map backend DTOs to UI items
  const mappedResults = useMemo(() => {
    if (isCommandMode) {
      const q = cleanQuery.toLowerCase();
      return COMMAND_ITEMS.filter(
        (c) => c.title.toLowerCase().includes(q) || (c.description && c.description.toLowerCase().includes(q))
      );
    }

    if (!apiResults?.content) return [];

    return apiResults.content
      .filter((item: SearchItemResponse) => {
        if (filterState.visibility !== "all" && item.visibility && item.visibility !== filterState.visibility) {
          return false;
        }
        return true;
      })
      .map((item: SearchItemResponse) => {
        let url = "";
        if (item.resultType === "GRAPH") {
          url = `/graph?workspaceId=${item.id}`;
        } else if (item.resultType === "NODE") {
          url = `/graph?workspaceId=${item.graphId}&nodeId=${item.id}`;
        }

        return {
          id: item.id,
          resultType: item.resultType,
          title: item.title,
          description: item.description,
          graphId: item.graphId,
          graphTitle: item.graphTitle,
          nodeTypeName: item.nodeTypeName,
          nodeTypeColor: item.nodeTypeColor,
          nodeTypeIcon: item.nodeTypeIcon,
          positionX: item.positionX,
          positionY: item.positionY,
          visibility: item.visibility,
          nodeCount: item.nodeCount,
          edgeCount: item.edgeCount,
          createdAt: item.createdAt,
          updatedAt: item.updatedAt,
          url
        } as SearchResultItem;
      });
  }, [apiResults, isCommandMode, cleanQuery, filterState.visibility]);

  // Reset selected index when results change
  useEffect(() => {
    setSelectedIndex(0);
  }, [mappedResults.length, query, filterState]);

  const addRecentSearch = useCallback((q: string) => {
    const trimmed = q.trim();
    if (!trimmed || trimmed.length < 1 || trimmed.startsWith(">")) return;

    setRecentSearches((prev) => {
      const filtered = prev.filter((item) => item.query.toLowerCase() !== trimmed.toLowerCase());
      const newItem: RecentSearchItem = {
        id: `rec-${Date.now()}`,
        query: trimmed,
        timestamp: Date.now()
      };
      return [newItem, ...filtered].slice(0, 10);
    });
  }, []);

  const removeRecentSearch = useCallback((id: string) => {
    setRecentSearches((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const clearRecentSearches = useCallback(() => {
    setRecentSearches([]);
  }, []);

  // Sync state to URL if on search page
  const updateUrlState = useCallback(
    (newQuery: string, newFilters: SearchFilterState) => {
      const params = new URLSearchParams();
      if (newQuery) params.set("q", newQuery);
      if (newFilters.category !== "all") params.set("type", newFilters.category);
      if (newFilters.nodeType !== "all") params.set("nodeType", newFilters.nodeType);
      if (newFilters.visibility !== "all") params.set("visibility", newFilters.visibility);

      setSearchParams(params, { replace: true });
    },
    [setSearchParams]
  );

  const handleSetQuery = useCallback(
    (q: string) => {
      setQuery(q);
      updateUrlState(q, filterState);
    },
    [filterState, updateUrlState]
  );

  const handleSetFilterState = useCallback(
    (updater: SearchFilterState | ((prev: SearchFilterState) => SearchFilterState)) => {
      setFilterState((prev) => {
        const next = typeof updater === "function" ? updater(prev) : updater;
        updateUrlState(query, next);
        return next;
      });
    },
    [query, updateUrlState]
  );

  // Group count numbers for tabs
  const categoryCounts = useMemo(() => {
    const allCount = apiResults?.totalElements ?? 0;
    const graphsCount = mappedResults.filter((r: SearchResultItem) => r.resultType === "GRAPH").length;
    const nodesCount = mappedResults.filter((r: SearchResultItem) => r.resultType === "NODE").length;

    return {
      all: allCount,
      graphs: filterState.category === "graphs" ? allCount : graphsCount,
      nodes: filterState.category === "nodes" ? allCount : nodesCount,
      commands: COMMAND_ITEMS.length
    };
  }, [apiResults, mappedResults, filterState.category]);

  return {
    query,
    setQuery: handleSetQuery,
    cleanQuery,
    isCommandMode,
    filterState,
    setFilterState: handleSetFilterState,
    recentSearches,
    suggestions: mappedResults,
    categoryCounts,
    selectedIndex,
    setSelectedIndex,
    addRecentSearch,
    removeRecentSearch,
    clearRecentSearches,
    isSearchLoading,
    isSearchError,
    searchError,
    refetchSearch: refetch
  };
}