export type VersionCommit = {
  id: string;
  hash: string;
  branch: string;
  message: string;
  author: string;
  authorInitials: string;
  timestamp: string;
  nodesAdded: number;
  nodesRemoved: number;
  edgesAdded: number;
  edgesRemoved: number;
  tags: string[];
  isMerge: boolean;
  parentIds: string[];
};

export type ForkGraphNode = {
  id: string;
  commitId: string;
  branch: string;
  x: number;
  y: number;
};

export type ForkGraphEdge = {
  from: string;
  to: string;
  type: "parent" | "merge";
};

export type CompareSelection = {
  left: string | null;
  right: string | null;
};

export type VersionHistoryState = {
  selectedCommitId: string | null;
  compareSelection: CompareSelection;
  isCompareOpen: boolean;
  isRestoreModalOpen: boolean;
  restoreCommitId: string | null;
};
