import type { Edge, Node } from "@xyflow/react";
import type { KnowledgeNodeData } from "../types";

export const initialNodes: Array<Node<KnowledgeNodeData, "knowledgeNode">> = [
  {
    id: "roadmap",
    type: "knowledgeNode",
    position: { x: 120, y: 120 },
    data: {
      title: "Product Roadmap",
      type: "Document",
      description: "Strategic bets, launch milestones, and feature themes.",
      confidence: 94,
      connections: 8
    }
  },
  {
    id: "signals",
    type: "knowledgeNode",
    position: { x: 520, y: 80 },
    data: {
      title: "Customer Signals",
      type: "Concept",
      description: "Aggregated insights from calls, tickets, and research.",
      confidence: 88,
      connections: 12
    }
  },
  {
    id: "maya",
    type: "knowledgeNode",
    position: { x: 340, y: 340 },
    data: {
      title: "Maya Chen",
      type: "Person",
      description: "Owns activation research and onboarding taxonomy.",
      confidence: 97,
      connections: 6
    }
  },
  {
    id: "pricing",
    type: "knowledgeNode",
    position: { x: 780, y: 320 },
    data: {
      title: "Pricing Decision",
      type: "Decision",
      description: "Approved packaging model for collaborative graph seats.",
      confidence: 91,
      connections: 5
    }
  }
];

export const initialEdges: Edge[] = [
  {
    id: "roadmap-signals",
    source: "roadmap",
    target: "signals",
    label: "references",
    animated: true,
    className: "knowledge-flow-edge"
  },
  {
    id: "signals-maya",
    source: "signals",
    target: "maya",
    label: "owned by",
    animated: true,
    className: "knowledge-flow-edge"
  },
  {
    id: "signals-pricing",
    source: "signals",
    target: "pricing",
    label: "informs",
    animated: true,
    className: "knowledge-flow-edge"
  },
  {
    id: "roadmap-pricing",
    source: "roadmap",
    target: "pricing",
    label: "depends on",
    className: "knowledge-flow-edge"
  }
];
