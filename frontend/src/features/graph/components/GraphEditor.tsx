import {
  Background,
  BackgroundVariant,
  Connection,
  Controls,
  Edge,
  MiniMap,
  Node,
  ReactFlow,
  ReactFlowProvider,
  useEdgesState,
  useNodesState,
  useReactFlow
} from "@xyflow/react";
import { AnimatePresence, motion } from "framer-motion";
import { Loader2, AlertTriangle, Plus, X, Sparkles, GripVertical, Link2 } from "lucide-react";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import "@xyflow/react/dist/style.css";
import { nodeTypeApi } from "../../../services";
import type { AutosaveState, KnowledgeNodeData } from "../types";
import type { NodeResponse } from "../../../services";
import type { NodeTypeResponse } from "../../../services";
import { useCreateNode, useDeleteNode, useCreateEdge, useDeleteEdge, useUpdateNode } from "../../../hooks/use-mutations";
import { useWorkspaceEdges, useWorkspaceNodes } from "../../../hooks/use-queries";
import { GraphContextMenu } from "./GraphContextMenu";
import { GraphInspector } from "./GraphInspector";
import { GraphToolbar } from "./GraphToolbar";
import { KeyboardShortcuts } from "./KeyboardShortcuts";
import { KnowledgeNode } from "./KnowledgeNode";
import { CreateEdgeModal } from "./CreateEdgeModal";
import { Button } from "../../../components/ui/button";
import { Input } from "../../../components/ui/input";
import { EmptyState } from "../../../components/ui/empty-state";
import { useToast } from "../../../components/ui/toast";
import { useGlobalSearch } from "../../search/hooks/useGlobalSearch";

type ContextMenuState = {
  x: number;
  y: number;
  flowX: number;
  flowY: number;
};

const nodeTypes = {
  knowledgeNode: KnowledgeNode
};

/**
 * Deterministic helper to calculate a non-overlapping graph coordinate for newly created nodes.
 */
export function getNextAvailableNodePosition(existingNodes: Array<{ position: { x: number; y: number } }>): { x: number; y: number } {
  if (!existingNodes || existingNodes.length === 0) {
    return { x: 100, y: 100 };
  }

  const startX = 100;
  const startY = 100;
  const stepX = 300;
  const stepY = 200;
  const maxCols = 3;

  for (let i = 0; i < 100; i++) {
    const col = i % maxCols;
    const row = Math.floor(i / maxCols);
    const candidateX = startX + col * stepX;
    const candidateY = startY + row * stepY;

    const isOccupied = existingNodes.some((n) => {
      const dx = Math.abs(n.position.x - candidateX);
      const dy = Math.abs(n.position.y - candidateY);
      return dx < 150 && dy < 100;
    });

    if (!isOccupied) {
      return { x: candidateX, y: candidateY };
    }
  }

  const count = existingNodes.length;
  return {
    x: startX + (count % maxCols) * stepX,
    y: startY + Math.floor(count / maxCols) * stepY
  };
}

function mapApiNodeToReactFlowNode(apiNode: NodeResponse): Node<KnowledgeNodeData, "knowledgeNode"> {
  const posX = typeof apiNode.positionX === "number" && Number.isFinite(apiNode.positionX)
    ? apiNode.positionX
    : typeof apiNode.attributes?.positionX === "number" && Number.isFinite(Number(apiNode.attributes.positionX))
    ? Number(apiNode.attributes.positionX)
    : 100;

  const posY = typeof apiNode.positionY === "number" && Number.isFinite(apiNode.positionY)
    ? apiNode.positionY
    : typeof apiNode.attributes?.positionY === "number" && Number.isFinite(Number(apiNode.attributes.positionY))
    ? Number(apiNode.attributes.positionY)
    : 100;

  const nodeTypeName = apiNode.nodeTypeName || (apiNode.attributes?.type as string) || "Concept";

  return {
    id: apiNode.id,
    type: "knowledgeNode",
    position: {
      x: posX,
      y: posY
    },
    data: {
      title: apiNode.label,
      type: nodeTypeName as any,
      description: (apiNode.attributes?.description as string) ?? "",
      confidence: (apiNode.attributes?.confidence as number) ?? 80,
      connections: (apiNode.attributes?.connections as number) ?? 0,
      version: apiNode.version
    }
  };
}

function mapApiEdgeToReactFlowEdge(apiEdge: any): Edge {
  const labelText = apiEdge.label || apiEdge.attributes?.label || apiEdge.relationshipType || apiEdge.edgeTypeName || "";
  return {
    id: apiEdge.id,
    source: apiEdge.sourceNodeId,
    target: apiEdge.targetNodeId,
    label: labelText,
    data: {
      relationshipType: apiEdge.relationshipType || apiEdge.edgeTypeName || "RELATED_TO",
      label: apiEdge.label || apiEdge.attributes?.label || "",
      description: apiEdge.description || apiEdge.attributes?.description || "",
      version: apiEdge.version,
      weight: apiEdge.weight
    },
    animated: true,
    className: "knowledge-flow-edge"
  };
}

function GraphEditorCanvas({ workspaceId }: { workspaceId: string }) {
  const { addToast } = useToast();
  const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);
  const [connectionMode, setConnectionMode] = useState(false);
  const [autosaveState, setAutosaveState] = useState<AutosaveState>("saved");
  const [inspectorWidth, setInspectorWidth] = useState(360);
  const [deleteConfirm, setDeleteConfirm] = useState<{ type: "node" | "edge"; id: string; label: string } | null>(null);

  // Track whether initial fitView has fired
  const hasFittedOnLoad = useRef(false);

  // Create Node Modal state
  const [isCreateNodeOpen, setIsCreateNodeOpen] = useState(false);
  const [createTitle, setCreateTitle] = useState("");
  const [createDescription, setCreateDescription] = useState("");
  const [createNodeTypeId, setCreateNodeTypeId] = useState<string>("");
  const [createPos, setCreatePos] = useState<{ x: number; y: number } | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);

  // Create Edge Modal state
  const [isCreateEdgeOpen, setIsCreateEdgeOpen] = useState(false);
  const [pendingConnection, setPendingConnection] = useState<{
    source: string;
    target: string;
    sourceTitle: string;
    targetTitle: string;
  } | null>(null);

  const {
    data: nodeListResponse,
    isLoading: isNodesLoading,
    isError: isNodesError,
    error: nodesError,
    refetch: refetchNodes
  } = useWorkspaceNodes(workspaceId);

  const {
    data: edgeListResponse,
    isLoading: isEdgesLoading,
    isError: isEdgesError,
    error: edgesError,
    refetch: refetchEdges
  } = useWorkspaceEdges(workspaceId);

  const nodes = useMemo(
    () => (nodeListResponse?.nodes ?? []).map(mapApiNodeToReactFlowNode),
    [nodeListResponse]
  );

  const edges = useMemo(
    () => (edgeListResponse ?? []).map(mapApiEdgeToReactFlowEdge),
    [edgeListResponse]
  );

  const [internalNodes, setNodes, onNodesChange] = useNodesState(nodes);
  const [internalEdges, setEdges, onEdgesChange] = useEdgesState(edges);

  // Sync edges cleanly with backend list
  useEffect(() => {
    setEdges(edges);
  }, [edges, setEdges]);

  // Reconcile server nodes with internalNodes without overwriting active drags or local state
  const prevNodeListRef = useRef<NodeResponse[] | null>(null);
  useEffect(() => {
    if (!nodeListResponse?.nodes) return;
    const serverNodes = nodeListResponse.nodes;

    if (prevNodeListRef.current === serverNodes) return;
    prevNodeListRef.current = serverNodes;

    setNodes((currentNodes) => {
      const currentMap = new Map(currentNodes.map((n) => [n.id, n]));
      return serverNodes.map((serverNode) => {
        const mapped = mapApiNodeToReactFlowNode(serverNode);
        const existingLocal = currentMap.get(serverNode.id);
        if (existingLocal) {
          return {
            ...mapped,
            selected: existingLocal.selected,
            dragging: existingLocal.dragging,
            position: existingLocal.dragging ? existingLocal.position : mapped.position
          };
        }
        return mapped;
      });
    });
  }, [nodeListResponse, setNodes]);

  const selectedNode = internalNodes.find((node) => node.selected);
  const selectedEdge = internalEdges.find((edge) => edge.selected);

  const selectedEdgeSourceNode = selectedEdge ? internalNodes.find((n) => n.id === selectedEdge.source) : undefined;
  const selectedEdgeTargetNode = selectedEdge ? internalNodes.find((n) => n.id === selectedEdge.target) : undefined;

  const reactFlow = useReactFlow();
  const { openSearch } = useGlobalSearch();
  const wrapperRef = useRef<HTMLDivElement | null>(null);

  const { data: nodeTypeListResponse } = useQuery<NodeTypeResponse[]>({
    queryKey: ["node-types", workspaceId],
    queryFn: () => nodeTypeApi.list(workspaceId).then((r) => r.data as any)
  });

  const createEdgeMutation = useCreateEdge(workspaceId);
  const deleteNodeMutation = useDeleteNode(workspaceId);
  const deleteEdgeMutation = useDeleteEdge(workspaceId);
  const updateNodeMutation = useUpdateNode(workspaceId);
  const addNodeMutation = useCreateNode(workspaceId);

  const onConnect = useCallback(
    (connection: Connection) => {
      if (!connection.source || !connection.target) return;
      if (connection.source === connection.target) {
        addToast({ type: "error", title: "Invalid Connection", description: "A topic cannot be connected to itself." });
        return;
      }

      const sourceNode = internalNodes.find((n) => n.id === connection.source);
      const targetNode = internalNodes.find((n) => n.id === connection.target);

      setPendingConnection({
        source: connection.source,
        target: connection.target,
        sourceTitle: sourceNode?.data.title || "Source Node",
        targetTitle: targetNode?.data.title || "Target Node"
      });
      setIsCreateEdgeOpen(true);
    },
    [internalNodes, addToast]
  );

  const handleCreateEdgeSubmit = (data: { relationshipType: string; label: string; description: string }) => {
    if (!pendingConnection) return;
    createEdgeMutation.mutate(
      {
        sourceNodeId: pendingConnection.source,
        targetNodeId: pendingConnection.target,
        relationshipType: data.relationshipType,
        label: data.label,
        description: data.description
      },
      {
        onSuccess: () => {
          setIsCreateEdgeOpen(false);
          setPendingConnection(null);
        }
      }
    );
  };

  const openCreateModal = (position?: { x: number; y: number }) => {
    setCreateTitle("");
    setCreateDescription("");
    setCreateNodeTypeId(nodeTypeListResponse?.[0]?.id || "");
    setCreatePos(position || null);
    setCreateError(null);
    setIsCreateNodeOpen(true);
  };

  const handleCreateNodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createTitle.trim()) {
      setCreateError("Title is required");
      return;
    }
    setCreateError(null);

    const targetPos = createPos || getNextAvailableNodePosition(internalNodes);
    const selectedTypeId = createNodeTypeId || nodeTypeListResponse?.[0]?.id;

    try {
      await addNodeMutation.mutateAsync({
        nodeTypeId: selectedTypeId,
        label: createTitle.trim(),
        positionX: targetPos.x,
        positionY: targetPos.y,
        attributes: {
          description: createDescription.trim(),
          confidence: 85,
          connections: 0,
          positionX: targetPos.x,
          positionY: targetPos.y
        }
      });
      setIsCreateNodeOpen(false);
    } catch (err: any) {
      setCreateError(err.message || "Failed to create node");
    }
  };

  const executeDelete = useCallback(() => {
    const selectedIds = new Set(internalNodes.filter((node) => node.selected).map((node) => node.id));
    const selectedEdgeIds = internalEdges.filter((edge) =>
      edge.selected || selectedIds.has(edge.source) || selectedIds.has(edge.target)
    ).map((edge) => edge.id);
    setNodes((currentNodes) => currentNodes.filter((node) => !selectedIds.has(node.id)));
    setEdges((currentEdges) =>
      currentEdges.filter((edge) => !selectedEdgeIds.includes(edge.id))
    );
    selectedIds.forEach((id) => {
      if (!id.startsWith("node-")) {
        deleteNodeMutation.mutate(id);
      }
    });
    selectedEdgeIds.forEach((id) => deleteEdgeMutation.mutate(id));
  }, [internalEdges, internalNodes, setEdges, setNodes, deleteNodeMutation, deleteEdgeMutation]);

  const deleteSelected = useCallback(() => {
    const selNode = internalNodes.find((n) => n.selected);
    const selEdge = internalEdges.find((e) => e.selected);
    if (selNode) {
      setDeleteConfirm({ type: "node", id: selNode.id, label: (selNode.data as KnowledgeNodeData).title || selNode.id });
    } else if (selEdge) {
      setDeleteConfirm({ type: "edge", id: selEdge.id, label: (selEdge.label as string) || selEdge.id });
    } else {
      executeDelete();
    }
  }, [internalNodes, internalEdges, executeDelete]);

  const fitView = useCallback(() => {
    reactFlow.fitView({ padding: 0.18, duration: 500 });
  }, [reactFlow]);

  // fitView once after initial data loads
  useEffect(() => {
    if (!hasFittedOnLoad.current && !isNodesLoading && !isEdgesLoading && internalNodes.length > 0) {
      hasFittedOnLoad.current = true;
      // Small delay to let React Flow measure nodes
      const timer = window.setTimeout(() => {
        reactFlow.fitView({ padding: 0.18, duration: 400 });
      }, 100);
      return () => { window.clearTimeout(timer); };
    }
    return undefined;
  }, [isNodesLoading, isEdgesLoading, internalNodes.length, reactFlow]);

  // Handle focusing target node passed via search parameter ?nodeId=...
  const [searchParams] = useSearchParams();
  const targetNodeId = searchParams.get("nodeId");
  const focusedNodeIdRef = useRef<string | null>(null);

  useEffect(() => {
    if (!targetNodeId || internalNodes.length === 0) return;
    if (focusedNodeIdRef.current === targetNodeId) return;

    const targetNode = internalNodes.find((n) => n.id === targetNodeId);
    if (!targetNode) return;

    focusedNodeIdRef.current = targetNodeId;
    hasFittedOnLoad.current = true;

    setNodes((current) =>
      current.map((n) => ({
        ...n,
        selected: n.id === targetNodeId
      }))
    );

    const timer = window.setTimeout(() => {
      reactFlow.setCenter(targetNode.position.x + 100, targetNode.position.y + 50, {
        zoom: 1.25,
        duration: 600
      });
    }, 150);

    return () => { window.clearTimeout(timer); };
  }, [targetNodeId, internalNodes, setNodes, reactFlow]);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      const target = event.target as HTMLElement | null;
      if (target?.tagName === "INPUT" || target?.tagName === "TEXTAREA") return;

      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "s") {
        event.preventDefault();
        setAutosaveState("saved");
      }

      if (event.key.toLowerCase() === "n") {
        event.preventDefault();
        openCreateModal();
      }

      if (event.key.toLowerCase() === "f") {
        event.preventDefault();
        fitView();
      }

      if ((event.key === "Delete" || event.key === "Backspace") && !deleteConfirm) {
        event.preventDefault();
        deleteSelected();
      }

      if (event.key === "Escape" && deleteConfirm) {
        setDeleteConfirm(null);
      }
    }

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [deleteSelected, fitView]);

  const onResizeStart = useCallback((event: React.PointerEvent<HTMLDivElement>) => {
    event.currentTarget.setPointerCapture(event.pointerId);
    const startX = event.clientX;
    const startWidth = inspectorWidth;

    function onPointerMove(moveEvent: PointerEvent) {
      const nextWidth = Math.min(520, Math.max(300, startWidth - (moveEvent.clientX - startX)));
      setInspectorWidth(nextWidth);
    }

    function onPointerUp() {
      window.removeEventListener("pointermove", onPointerMove);
      window.removeEventListener("pointerup", onPointerUp);
    }

    window.addEventListener("pointermove", onPointerMove);
    window.addEventListener("pointerup", onPointerUp);
  }, [inspectorWidth]);

  const minimapNodeColor = useCallback((node: Node) => {
    const data = node.data as KnowledgeNodeData;
    if (data.type === "Document") return "#a78bfa";
    if (data.type === "Person") return "#6ee7b7";
    if (data.type === "Decision") return "#fcd34d";
    return "#22d3ee";
  }, []);

  const defaultEdgeOptions = useMemo(
    () => ({
      type: "smoothstep",
      animated: true,
      className: "knowledge-flow-edge",
      style: { strokeWidth: 2 }
    }),
    []
  );

  const isLoading = isNodesLoading || isEdgesLoading;
  const isError = isNodesError || isEdgesError;

  if (isError) {
    return (
      <div className="flex h-[calc(100vh-112px)] min-h-[720px] items-center justify-center">
        <EmptyState
          icon={<AlertTriangle size={28} className="text-rose-300" />}
          title="Unable to load graph"
          description={(nodesError ?? edgesError)?.message || "Failed to fetch workspace data."}
          action={
            <Button variant="secondary" onClick={() => { refetchNodes(); refetchEdges(); }}>
              Retry
            </Button>
          }
        />
      </div>
    );
  }

  return (
    <div className="flex h-[calc(100vh-112px)] min-h-[720px] overflow-hidden rounded-2xl border border-white/10 bg-[rgba(5,7,11,0.78)] shadow-glass backdrop-blur-xl">
      <div ref={wrapperRef} className="relative min-w-0 flex-1">
        {isLoading && (
          <div className="absolute inset-0 z-30 flex items-center justify-center bg-[rgba(5,7,11,0.8)]">
            <div className="flex items-center gap-2 text-sm text-cyan-300">
              <Loader2 size={18} className="animate-spin" />
              Loading graph...
            </div>
          </div>
        )}
        <div className="pointer-events-none absolute left-4 top-4 z-20">
          <GraphToolbar
            autosaveState={autosaveState}
            onAddNode={() => openCreateModal()}
            onConnect={() => setConnectionMode(true)}
            onSelect={() => {
              setConnectionMode(false);
              setNodes((currentNodes) => currentNodes.map((node) => ({ ...node, selected: false })));
              setEdges((currentEdges) => currentEdges.map((edge) => ({ ...edge, selected: false })));
            }}
            onFitView={fitView}
            onDeleteSelected={deleteSelected}
            onSearch={openSearch}
          />
        </div>

        <ReactFlow
          nodes={internalNodes}
          edges={internalEdges}
          nodeTypes={nodeTypes}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          nodesConnectable={connectionMode}
          onNodeDragStop={(_, node) => {
            const typedNode = node as Node<KnowledgeNodeData, "knowledgeNode">;
            if (!typedNode.id.startsWith("node-") && typedNode.data.version !== undefined) {
              setAutosaveState("saving");
              updateNodeMutation.mutate({
                nodeId: typedNode.id,
                label: typedNode.data.title,
                positionX: typedNode.position.x,
                positionY: typedNode.position.y,
                attributes: {
                  description: typedNode.data.description,
                  confidence: typedNode.data.confidence,
                  connections: typedNode.data.connections,
                  positionX: typedNode.position.x,
                  positionY: typedNode.position.y
                },
                version: typedNode.data.version ?? 0
              }, {
                onSuccess: () => setAutosaveState("saved"),
                onError: (error: any) => {
                  setAutosaveState("saved");
                  if (error?.status === 409 || error?.code === "CONFLICT") {
                    addToast({ type: "error", title: "Position conflict", description: "This node was changed elsewhere. Refresh and try again." });
                  }
                }
              });
            }
          }}
          onConnect={onConnect}
          defaultEdgeOptions={defaultEdgeOptions}
          proOptions={{ hideAttribution: true }}
          minZoom={0.18}
          maxZoom={1.8}
          onPaneClick={() => setContextMenu(null)}
          onNodeContextMenu={(event, node) => {
            event.preventDefault();
            setNodes((currentNodes) => currentNodes.map((item) => ({ ...item, selected: item.id === node.id })));
            setEdges((currentEdges) => currentEdges.map((e) => ({ ...e, selected: false })));
            setContextMenu({
              x: event.clientX,
              y: event.clientY,
              flowX: node.position.x + 40,
              flowY: node.position.y + 40
            });
          }}
          onPaneContextMenu={(event) => {
            event.preventDefault();
            const position = reactFlow.screenToFlowPosition({ x: event.clientX, y: event.clientY });
            setContextMenu({ x: event.clientX, y: event.clientY, flowX: position.x, flowY: position.y });
          }}
          className="knowledge-flow"
        >
          <Background variant={BackgroundVariant.Lines} gap={32} size={1} color="rgba(148,163,184,0.18)" />
          <Controls
            className="!bottom-4 !right-4 !left-auto !top-auto overflow-hidden !rounded-xl !border !border-white/10 !bg-[rgba(8,12,20,0.78)] !shadow-glass !backdrop-blur-2xl"
          />
          <MiniMap
            nodeColor={minimapNodeColor}
            maskColor="rgba(2,6,23,0.72)"
            className="!bottom-4 !right-20 !h-32 !w-48 overflow-hidden !rounded-xl !border !border-white/10 !bg-[rgba(8,12,20,0.78)] !shadow-glass !backdrop-blur-2xl"
          />
        </ReactFlow>

        <KeyboardShortcuts />

        <AnimatePresence>
          {contextMenu && (
            <GraphContextMenu
              x={contextMenu.x}
              y={contextMenu.y}
              onAddNode={() => openCreateModal({ x: contextMenu.flowX, y: contextMenu.flowY })}
              onSearch={openSearch}
              onClearSelection={() => {
                setNodes((currentNodes) => currentNodes.map((node) => ({ ...node, selected: false })));
                setEdges((currentEdges) => currentEdges.map((edge) => ({ ...edge, selected: false })));
              }}
              onClose={() => setContextMenu(null)}
            />
          )}
        </AnimatePresence>

        {/* Empty graph guided experience */}
        {!isLoading && internalNodes.length === 0 && (
          <div className="absolute inset-0 z-10 flex items-center justify-center pointer-events-none">
            <div className="pointer-events-auto max-w-md text-center space-y-5 rounded-2xl border border-white/10 bg-[rgba(8,12,20,0.92)] p-8 shadow-glass backdrop-blur-2xl">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-400/20 to-violet-500/20 border border-cyan-400/30">
                <Sparkles size={24} className="text-cyan-300" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-slate-100">Your graph is empty</h3>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Start by adding your first idea, concept, document, person, or decision.
                </p>
              </div>
              <Button
                className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-2"
                onClick={() => openCreateModal()}
              >
                <Plus size={16} />
                Add First Node
              </Button>
              <div className="space-y-2 pt-3 border-t border-white/10 text-left">
                <p className="text-xs font-semibold uppercase text-slate-400 tracking-wider">Quick start</p>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <span className="flex h-5 w-5 items-center justify-center rounded-md border border-white/10 bg-white/5 text-[10px] font-bold text-cyan-300">1</span>
                  Add a node to capture an idea
                </div>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <span className="flex h-5 w-5 items-center justify-center rounded-md border border-white/10 bg-white/5 text-[10px] font-bold text-cyan-300">2</span>
                  <GripVertical size={11} className="inline text-slate-500" /> Drag nodes to organize
                </div>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <span className="flex h-5 w-5 items-center justify-center rounded-md border border-white/10 bg-white/5 text-[10px] font-bold text-cyan-300">3</span>
                  <Link2 size={11} className="inline text-slate-500" /> Connect related nodes
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <GraphInspector
        width={inspectorWidth}
        selectedNode={selectedNode}
        selectedEdge={selectedEdge}
        sourceNodeTitle={selectedEdgeSourceNode?.data.title}
        targetNodeTitle={selectedEdgeTargetNode?.data.title}
        workspaceId={workspaceId}
        onResizeStart={onResizeStart}
      />

      {/* CREATE RELATIONSHIP EDGE MODAL */}
      <CreateEdgeModal
        isOpen={isCreateEdgeOpen}
        onClose={() => {
          setIsCreateEdgeOpen(false);
          setPendingConnection(null);
        }}
        sourceNodeTitle={pendingConnection?.sourceTitle || ""}
        targetNodeTitle={pendingConnection?.targetTitle || ""}
        onSubmit={handleCreateEdgeSubmit}
        isLoading={createEdgeMutation.isPending}
      />

      {/* CREATE NODE MODAL */}
      <AnimatePresence>
        {isCreateNodeOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="w-full max-w-md overflow-hidden rounded-2xl border border-white/10 bg-[rgba(10,15,28,0.96)] p-6 shadow-2xl backdrop-blur-2xl"
            >
              <div className="flex items-center justify-between border-b border-white/10 pb-3">
                <div className="flex items-center gap-2 text-cyan-300">
                  <Plus size={18} />
                  <h3 className="text-lg font-semibold text-slate-100">Create Knowledge Node</h3>
                </div>
                <button
                  type="button"
                  onClick={() => setIsCreateNodeOpen(false)}
                  className="rounded-lg p-1 text-muted-foreground hover:bg-white/10 hover:text-white"
                >
                  <X size={18} />
                </button>
              </div>

              <form onSubmit={handleCreateNodeSubmit} className="mt-4 space-y-4">
                {createError && (
                  <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-300">
                    {createError}
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
                    Node Title / Label <span className="text-rose-400">*</span>
                  </label>
                  <Input
                    value={createTitle}
                    onChange={(e) => setCreateTitle(e.target.value)}
                    placeholder="e.g. Distributed Consensus Protocol"
                    className="mt-1.5"
                    maxLength={255}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
                    Description / Content
                  </label>
                  <textarea
                    value={createDescription}
                    onChange={(e) => setCreateDescription(e.target.value)}
                    placeholder="Notes, references, or key findings..."
                    rows={3}
                    maxLength={2000}
                    className="mt-1.5 w-full rounded-xl border border-white/10 bg-white/[0.04] p-3 text-sm text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
                  />
                </div>

                {nodeTypeListResponse && nodeTypeListResponse.length > 0 && (
                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
                      Node Type
                    </label>
                    <div className="grid grid-cols-2 gap-2">
                      {nodeTypeListResponse.map((nt) => (
                        <button
                          key={nt.id}
                          type="button"
                          onClick={() => setCreateNodeTypeId(nt.id)}
                          className={`flex items-center gap-2 rounded-xl border p-2.5 text-xs font-medium transition ${
                            createNodeTypeId === nt.id
                              ? "border-cyan-400 bg-cyan-400/10 text-cyan-100"
                              : "border-white/10 bg-white/[0.03] text-slate-300 hover:bg-white/[0.06]"
                          }`}
                        >
                          <span
                            className="h-2.5 w-2.5 rounded-full"
                            style={{ backgroundColor: nt.colorCode || "#22D3EE" }}
                          />
                          {nt.name}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div className="mt-6 flex justify-end gap-2 border-t border-white/10 pt-4">
                  <Button type="button" variant="ghost" onClick={() => setIsCreateNodeOpen(false)}>
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    disabled={addNodeMutation.isPending}
                    className="bg-cyan-500 text-slate-950 font-semibold hover:bg-cyan-400 gap-2"
                  >
                    {addNodeMutation.isPending && <Loader2 size={15} className="animate-spin" />}
                    Create Node
                  </Button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* DELETE CONFIRMATION MODAL */}
      <AnimatePresence>
        {deleteConfirm && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="w-full max-w-sm rounded-2xl border border-white/10 bg-[rgba(10,15,28,0.96)] p-6 shadow-2xl backdrop-blur-2xl"
            >
              <h3 className="text-lg font-semibold text-slate-100">Delete {deleteConfirm.type}?</h3>
              <p className="mt-2 text-sm text-muted-foreground">
                Are you sure you want to delete <span className="font-medium text-slate-200">&ldquo;{deleteConfirm.label}&rdquo;</span>?
                {deleteConfirm.type === "node" && " Connected edges will also be removed."}
                {" "}This action cannot be undone.
              </p>
              <div className="mt-5 flex justify-end gap-2">
                <Button variant="ghost" onClick={() => setDeleteConfirm(null)}>Cancel</Button>
                <Button
                  className="bg-rose-500 text-white font-semibold hover:bg-rose-400 gap-2"
                  onClick={() => { executeDelete(); setDeleteConfirm(null); }}
                >
                  Delete
                </Button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}

export function GraphEditor({ workspaceId }: { workspaceId: string }) {
  return (
    <ReactFlowProvider>
      <GraphEditorCanvas workspaceId={workspaceId} />
    </ReactFlowProvider>
  );
}
