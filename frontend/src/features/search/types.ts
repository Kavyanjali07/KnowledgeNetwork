export type SearchResultCategory =
  | "all"
  | "graphs"
  | "nodes"
  | "commands";

export type NodeTypeFilter = "all" | "Concept" | "Document" | "Person" | "Decision" | "Service" | "Feature";
export type VisibilityFilter = "all" | "PUBLIC" | "PRIVATE";

export interface SearchResultItem {
  id: string;
  resultType: "GRAPH" | "NODE" | "COMMAND";
  title: string;
  description?: string;
  graphId?: string;
  graphTitle?: string;
  nodeTypeName?: string;
  nodeTypeColor?: string;
  nodeTypeIcon?: string;
  positionX?: number;
  positionY?: number;
  visibility?: "PUBLIC" | "PRIVATE";
  nodeCount?: number;
  edgeCount?: number;
  createdAt?: string;
  updatedAt?: string;
  url?: string;
  actionId?: string;
}

export interface SearchFilterState {
  category: SearchResultCategory;
  nodeType: NodeTypeFilter;
  visibility: VisibilityFilter;
}

export interface RecentSearchItem {
  id: string;
  query: string;
  timestamp: number;
}
