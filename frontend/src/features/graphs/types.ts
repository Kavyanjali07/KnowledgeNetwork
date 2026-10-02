import type { LicenseType } from "../../services";

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
  licenseType?: LicenseType;
  isPublished?: boolean;
  publishedAt?: string;
  customAttribution?: string;
  derivativeCount?: number;
  referenceCount?: number;
  updatedAt: string;
  version?: number;
  nodes: number;
  edges: number;
  accent: string;
  height: "short" | "medium" | "tall";
};
