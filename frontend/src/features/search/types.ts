export type SearchResultCategory =
  | "all"
  | "nodes"
  | "edges"
  | "tags"
  | "people"
  | "workspaces"
  | "commands";

export type DateRangeOption = "any" | "today" | "week" | "month";
export type SortOption = "relevance" | "recent" | "confidence" | "connections";

export interface SearchResultItem {
  id: string;
  category: SearchResultCategory;
  title: string;
  subtitle?: string;
  description?: string;
  tags?: string[];
  type?: string; // e.g. Concept, Document, Decision, Person, Service, Feature, Edge
  confidence?: number;
  connections?: number;
  updatedAt?: string;
  url?: string;
  sourceNode?: string;
  targetNode?: string;
  shortcut?: string;
  actionId?: string;
}

export interface SearchFilterState {
  category: SearchResultCategory;
  selectedTag?: string;
  dateRange: DateRangeOption;
  minConfidence: number;
  sortBy: SortOption;
}

export interface RecentSearchItem {
  id: string;
  query: string;
  timestamp: number;
  category?: SearchResultCategory;
}

export interface TagInfo {
  name: string;
  count: number;
  category?: string;
}
