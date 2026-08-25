import type { ManagedGraph, GraphVisibility } from "../types";
import type { GraphResponse, WorkspaceResponse } from "../../../services";

const accents = [
  "from-cyan-300 to-violet-400",
  "from-emerald-300 to-cyan-400",
  "from-amber-300 to-violet-400",
  "from-fuchsia-300 to-cyan-400",
  "from-blue-300 to-emerald-300",
  "from-violet-300 to-rose-300"
] as const;

function getAccent(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) {
    hash = id.charCodeAt(i) + ((hash << 5) - hash);
  }
  return accents[Math.abs(hash) % accents.length] as string;
}

export function mapGraphResponseToManagedGraph(graph: GraphResponse | WorkspaceResponse): ManagedGraph {
  const ownerName = graph.ownerName?.trim() || graph.ownerEmail || "Unknown";
  const initials = ownerName
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  const title = (graph as GraphResponse).title || (graph as WorkspaceResponse).name || "Untitled Graph";
  const rawVisibility = graph.visibility || "PRIVATE";
  const visibility: GraphVisibility = rawVisibility === "PUBLIC" ? "Public" : "Private";

  const nodeCount = (graph as GraphResponse).nodeCount ?? 0;
  const edgeCount = (graph as GraphResponse).edgeCount ?? 0;

  return {
    id: graph.id,
    title,
    description: graph.description || "",
    ownerId: graph.ownerId,
    owner: {
      name: ownerName,
      initials: initials || "?"
    },
    tags: [],
    likes: 0,
    forks: 0,
    visibility,
    rawVisibility,
    updatedAt: graph.updatedAt,
    version: graph.version,
    nodes: nodeCount,
    edges: edgeCount,
    accent: getAccent(graph.id),
    height: "medium"
  };
}

export const mapWorkspaceResponseToManagedGraph = mapGraphResponseToManagedGraph;
