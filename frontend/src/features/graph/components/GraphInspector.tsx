import type { Edge, Node } from "@xyflow/react";
import { motion } from "framer-motion";
import { Activity, GitBranch, Info, PanelRightClose, Save, Trash2, Edit3, Loader2, Link2 } from "lucide-react";
import React, { useEffect, useState } from "react";
import type { KnowledgeNodeData } from "../types";
import { Button } from "../../../components/ui/button";
import { Input } from "../../../components/ui/input";
import { useUpdateNode, useDeleteNode, useUpdateEdge, useDeleteEdge } from "../../../hooks/use-mutations";

type GraphInspectorProps = {
  width: number;
  selectedNode?: Node;
  selectedEdge?: Edge;
  sourceNodeTitle?: string;
  targetNodeTitle?: string;
  workspaceId: string;
  onResizeStart: (event: React.PointerEvent<HTMLDivElement>) => void;
};

const RELATIONSHIP_TYPES = [
  { value: "RELATED_TO", label: "Related to" },
  { value: "DEPENDS_ON", label: "Depends on" },
  { value: "PART_OF", label: "Part of" },
  { value: "PREREQUISITE_OF", label: "Prerequisite of" },
  { value: "CAUSES", label: "Causes" },
  { value: "SUPPORTS", label: "Supports" },
  { value: "CONTRADICTS", label: "Contradicts" },
  { value: "REFERENCES", label: "References" }
];

export function GraphInspector({
  width,
  selectedNode,
  selectedEdge,
  sourceNodeTitle,
  targetNodeTitle,
  workspaceId,
  onResizeStart
}: GraphInspectorProps) {
  const nodeData = selectedNode?.data as KnowledgeNodeData | undefined;
  const edgeData = selectedEdge?.data as any;

  const [isEditing, setIsEditing] = useState(false);
  const [nodeTitle, setNodeTitle] = useState("");
  const [nodeDescription, setNodeDescription] = useState("");
  
  // Edge edit fields
  const [edgeType, setEdgeType] = useState("RELATED_TO");
  const [edgeLabel, setEdgeLabel] = useState("");
  const [edgeDescription, setEdgeDescription] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const updateNodeMutation = useUpdateNode(workspaceId);
  const deleteNodeMutation = useDeleteNode(workspaceId);

  const updateEdgeMutation = useUpdateEdge(workspaceId);
  const deleteEdgeMutation = useDeleteEdge(workspaceId);

  useEffect(() => {
    if (selectedNode && nodeData) {
      setNodeTitle(nodeData.title || "");
      setNodeDescription(nodeData.description || "");
      setIsEditing(false);
      setError(null);
      setConfirmDelete(false);
    } else if (selectedEdge) {
      setEdgeType(edgeData?.relationshipType || "RELATED_TO");
      setEdgeLabel(edgeData?.label || (selectedEdge.label as string) || "");
      setEdgeDescription(edgeData?.description || "");
      setIsEditing(false);
      setError(null);
      setConfirmDelete(false);
    }
  }, [selectedNode?.id, selectedEdge?.id, nodeData?.title, nodeData?.description, edgeData?.relationshipType, edgeData?.label, edgeData?.description]);

  const handleSaveNode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedNode || !nodeTitle.trim()) {
      setError("Title is required");
      return;
    }
    setError(null);
    try {
      await updateNodeMutation.mutateAsync({
        nodeId: selectedNode.id,
        label: nodeTitle.trim(),
        positionX: selectedNode.position.x,
        positionY: selectedNode.position.y,
        attributes: {
          description: nodeDescription.trim(),
          confidence: nodeData?.confidence ?? 50,
          connections: nodeData?.connections ?? 0,
          positionX: selectedNode.position.x,
          positionY: selectedNode.position.y
        },
        version: nodeData?.version ?? 1
      });
      setIsEditing(false);
    } catch (err: any) {
      if (err?.status === 409 || err?.code === "CONFLICT") {
        setError("This item was changed elsewhere. Refresh the latest version and try again.");
      } else {
        setError(err.message || "Failed to update node");
      }
    }
  };

  const handleSaveEdge = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEdge) return;
    setError(null);
    try {
      await updateEdgeMutation.mutateAsync({
        edgeId: selectedEdge.id,
        relationshipType: edgeType,
        label: edgeLabel.trim(),
        description: edgeDescription.trim(),
        version: edgeData?.version ?? 0
      });
      setIsEditing(false);
    } catch (err: any) {
      if (err?.status === 409 || err?.code === "CONFLICT") {
        setError("This connection was changed elsewhere. Refresh the latest version and try again.");
      } else {
        setError(err.message || "Failed to update connection");
      }
    }
  };

  const handleDeleteNode = async () => {
    if (!selectedNode) return;
    if (!confirmDelete) {
      setConfirmDelete(true);
      return;
    }
    try {
      await deleteNodeMutation.mutateAsync(selectedNode.id);
      setConfirmDelete(false);
    } catch (err: any) {
      setError(err.message || "Failed to delete node");
      setConfirmDelete(false);
    }
  };

  const handleDeleteEdge = async () => {
    if (!selectedEdge) return;
    if (!confirmDelete) {
      setConfirmDelete(true);
      return;
    }
    try {
      await deleteEdgeMutation.mutateAsync(selectedEdge.id);
      setConfirmDelete(false);
    } catch (err: any) {
      setError(err.message || "Failed to delete connection");
      setConfirmDelete(false);
    }
  };

  return (
    <motion.aside
      className="relative hidden shrink-0 border-l border-white/10 bg-[rgba(8,12,20,0.76)] shadow-glass backdrop-blur-2xl xl:block"
      style={{ width }}
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.25 }}
    >
      <div
        className="absolute left-0 top-0 h-full w-1 cursor-col-resize bg-transparent transition hover:bg-cyan-300/30"
        onPointerDown={onResizeStart}
      />
      <div className="flex h-full flex-col p-4 overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-muted-foreground">Inspector</p>
            <h2 className="mt-1 text-lg font-semibold">
              {selectedNode ? "Node details" : selectedEdge ? "Relationship details" : "Inspector"}
            </h2>
          </div>
          <PanelRightClose size={18} className="text-muted-foreground" />
        </div>

        {selectedNode && nodeData ? (
          /* NODE INSPECTOR */
          <div className="mt-6 space-y-4">
            {error && (
              <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-300">
                {error}
              </div>
            )}

            {!isEditing ? (
              <div className="rounded-xl border border-white/10 bg-white/[0.045] p-4">
                <div className="flex items-center justify-between">
                  <span className="rounded-full border border-cyan-400/30 bg-cyan-400/10 px-2.5 py-0.5 text-xs font-medium text-cyan-200">
                    {nodeData.type || "Concept"}
                  </span>
                  <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="flex items-center gap-1 text-xs text-amber-300 hover:underline"
                  >
                    <Edit3 size={13} /> Edit
                  </button>
                </div>
                <h3 className="mt-3 text-xl font-semibold text-slate-100">{nodeData.title}</h3>
                <p className="mt-3 text-sm leading-6 text-slate-300">
                  {nodeData.description || "No description provided."}
                </p>
              </div>
            ) : (
              <form onSubmit={handleSaveNode} className="rounded-xl border border-cyan-400/30 bg-white/[0.05] p-4 space-y-3">
                <div className="flex items-center justify-between border-b border-white/10 pb-2">
                  <span className="text-xs font-semibold uppercase text-cyan-300">Editing Node</span>
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="text-xs text-muted-foreground hover:text-white"
                  >
                    Cancel
                  </button>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Title / Label *</label>
                  <Input
                    value={nodeTitle}
                    onChange={(e) => setNodeTitle(e.target.value)}
                    className="mt-1 h-9 text-sm"
                    maxLength={255}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Description / Content</label>
                  <textarea
                    value={nodeDescription}
                    onChange={(e) => setNodeDescription(e.target.value)}
                    rows={3}
                    maxLength={2000}
                    className="mt-1 w-full rounded-lg border border-white/10 bg-white/[0.04] p-2 text-xs text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
                  />
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <Button
                    type="submit"
                    disabled={updateNodeMutation.isPending}
                    className="h-8 gap-1.5 bg-cyan-500 text-xs font-semibold text-slate-950 hover:bg-cyan-400"
                  >
                    {updateNodeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Save size={13} />}
                    Save
                  </Button>
                </div>
              </form>
            )}

            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-xl border border-white/10 bg-black/18 p-3">
                <Activity size={15} className="text-cyan-200" />
                <p className="mt-2 text-xl font-semibold text-slate-100">{nodeData.confidence}%</p>
                <p className="mt-0.5 text-xs text-muted-foreground">Confidence</p>
              </div>
              <div className="rounded-xl border border-white/10 bg-black/18 p-3">
                <GitBranch size={15} className="text-violet-200" />
                <p className="mt-2 text-xl font-semibold text-slate-100">{nodeData.connections}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">Connections</p>
              </div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/[0.045] p-4">
              <p className="text-xs font-semibold text-muted-foreground uppercase">Metadata</p>
              <div className="mt-3 space-y-2 text-xs">
                <div className="flex justify-between text-muted-foreground">
                  <span>ID</span>
                  <span className="font-mono text-[11px] text-slate-300 truncate max-w-[160px]">{selectedNode.id}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Position X</span>
                  <span className="font-mono text-slate-300">{Math.round(selectedNode.position.x ?? 0)}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Position Y</span>
                  <span className="font-mono text-slate-300">{Math.round(selectedNode.position.y ?? 0)}</span>
                </div>
              </div>

              <div className="mt-4 border-t border-white/10 pt-3">
                {confirmDelete ? (
                  <div className="space-y-2">
                    <p className="text-xs text-rose-300">Delete this node and its connections?</p>
                    <div className="flex gap-2">
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => setConfirmDelete(false)}
                        className="flex-1 h-7 text-xs"
                      >
                        Cancel
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={handleDeleteNode}
                        disabled={deleteNodeMutation.isPending}
                        className="flex-1 h-7 text-xs font-semibold text-rose-300 hover:bg-rose-500/10"
                      >
                        {deleteNodeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                        Confirm
                      </Button>
                    </div>
                  </div>
                ) : (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={handleDeleteNode}
                    disabled={deleteNodeMutation.isPending}
                    className="w-full h-8 justify-center gap-2 text-xs font-semibold text-rose-300 hover:bg-rose-500/10 hover:text-rose-200"
                  >
                    {deleteNodeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                    Delete Node
                  </Button>
                )}
              </div>
            </div>
          </div>
        ) : selectedEdge ? (
          /* EDGE INSPECTOR */
          <div className="mt-6 space-y-4">
            {error && (
              <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 p-2.5 text-xs text-rose-300">
                {error}
              </div>
            )}

            {!isEditing ? (
              <div className="rounded-xl border border-white/10 bg-white/[0.045] p-4">
                <div className="flex items-center justify-between">
                  <span className="rounded-full border border-cyan-400/30 bg-cyan-400/10 px-2.5 py-0.5 text-xs font-medium text-cyan-200 flex items-center gap-1">
                    <Link2 size={12} /> {edgeType}
                  </span>
                  <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="flex items-center gap-1 text-xs text-amber-300 hover:underline"
                  >
                    <Edit3 size={13} /> Edit
                  </button>
                </div>

                <div className="mt-3 flex items-center gap-2 text-xs text-slate-300 bg-slate-950/60 p-2.5 rounded-lg border border-slate-800">
                  <span className="font-medium text-cyan-400 truncate max-w-[120px]">{sourceNodeTitle || "Source"}</span>
                  <span className="text-cyan-500 font-bold">&rarr;</span>
                  <span className="font-medium text-cyan-400 truncate max-w-[120px]">{targetNodeTitle || "Target"}</span>
                </div>

                {edgeLabel && (
                  <h3 className="mt-3 text-lg font-semibold text-slate-100">{edgeLabel}</h3>
                )}
                <p className="mt-2 text-sm leading-6 text-slate-300">
                  {edgeDescription || "No description provided for this connection."}
                </p>
              </div>
            ) : (
              <form onSubmit={handleSaveEdge} className="rounded-xl border border-cyan-400/30 bg-white/[0.05] p-4 space-y-3">
                <div className="flex items-center justify-between border-b border-white/10 pb-2">
                  <span className="text-xs font-semibold uppercase text-cyan-300">Editing Relationship</span>
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="text-xs text-muted-foreground hover:text-white"
                  >
                    Cancel
                  </button>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Relationship Type</label>
                  <select
                    value={edgeType}
                    onChange={(e) => setEdgeType(e.target.value)}
                    className="mt-1 w-full rounded-lg border border-white/10 bg-slate-950 p-2 text-xs text-slate-100 focus:border-cyan-400 focus:outline-none"
                  >
                    {RELATIONSHIP_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Connection Label</label>
                  <Input
                    value={edgeLabel}
                    onChange={(e) => setEdgeLabel(e.target.value)}
                    placeholder="e.g. requires, leads to"
                    className="mt-1 h-9 text-sm"
                    maxLength={255}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Description / Rationale</label>
                  <textarea
                    value={edgeDescription}
                    onChange={(e) => setEdgeDescription(e.target.value)}
                    rows={3}
                    maxLength={2000}
                    className="mt-1 w-full rounded-lg border border-white/10 bg-white/[0.04] p-2 text-xs text-slate-100 placeholder:text-muted-foreground focus:border-cyan-400 focus:outline-none"
                  />
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <Button
                    type="submit"
                    disabled={updateEdgeMutation.isPending}
                    className="h-8 gap-1.5 bg-cyan-500 text-xs font-semibold text-slate-950 hover:bg-cyan-400"
                  >
                    {updateEdgeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Save size={13} />}
                    Save
                  </Button>
                </div>
              </form>
            )}

            <div className="rounded-xl border border-white/10 bg-white/[0.045] p-4">
              <p className="text-xs font-semibold text-muted-foreground uppercase">Metadata</p>
              <div className="mt-3 space-y-2 text-xs">
                <div className="flex justify-between text-muted-foreground">
                  <span>Edge ID</span>
                  <span className="font-mono text-[11px] text-slate-300 truncate max-w-[160px]">{selectedEdge.id}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Source ID</span>
                  <span className="font-mono text-[11px] text-slate-300 truncate max-w-[160px]">{selectedEdge.source}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Target ID</span>
                  <span className="font-mono text-[11px] text-slate-300 truncate max-w-[160px]">{selectedEdge.target}</span>
                </div>
              </div>

              <div className="mt-4 border-t border-white/10 pt-3">
                {confirmDelete ? (
                  <div className="space-y-2">
                    <p className="text-xs text-rose-300">Delete this connection?</p>
                    <div className="flex gap-2">
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => setConfirmDelete(false)}
                        className="flex-1 h-7 text-xs"
                      >
                        Cancel
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={handleDeleteEdge}
                        disabled={deleteEdgeMutation.isPending}
                        className="flex-1 h-7 text-xs font-semibold text-rose-300 hover:bg-rose-500/10"
                      >
                        {deleteEdgeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                        Confirm
                      </Button>
                    </div>
                  </div>
                ) : (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={handleDeleteEdge}
                    disabled={deleteEdgeMutation.isPending}
                    className="w-full h-8 justify-center gap-2 text-xs font-semibold text-rose-300 hover:bg-rose-500/10 hover:text-rose-200"
                  >
                    {deleteEdgeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                    Delete Relationship
                  </Button>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* EMPTY STATE */
          <div className="mt-6 rounded-xl border border-white/10 bg-white/[0.04] p-4">
            <Info size={18} className="text-cyan-200" />
            <h3 className="mt-4 font-semibold text-slate-100">Select a node or edge</h3>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              Click any graph node or relationship edge to inspect, edit details, or manage connections.
            </p>
          </div>
        )}
      </div>
    </motion.aside>
  );
}
