export type GraphVisibility = "Public" | "Workspace" | "Private";

export type ManagedGraph = {
  id: string;
  title: string;
  description: string;
  ownerId?: string;
  owner: {
    name: string;
    initials: string;
  };
  tags: string[];
  likes: number;
  forks: number;
  visibility: GraphVisibility;
  rawVisibility?: 'PUBLIC' | 'PRIVATE';
  updatedAt: string;
  version?: number;
  nodes: number;
  edges: number;
  accent: string;
  height: "short" | "medium" | "tall";
};
