export type KnowledgeNodeData = {
  title: string;
  type: "Concept" | "Document" | "Person" | "Decision";
  description: string;
  confidence: number;
  connections: number;
  version?: number;
};

export type AutosaveState = "saved" | "saving" | "idle";
