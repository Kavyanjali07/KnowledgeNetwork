import type { Node } from "@xyflow/react";
import { motion } from "framer-motion";
import { Activity, GitBranch, Info, PanelRightClose, Save, Trash2, Edit3, Loader2 } from "lucide-react";
import { useEffect, useState } from "react";
import type { KnowledgeNodeData } from "../types";
import { Button } from "../../../components/ui/button";
import { Input } from "../../../components/ui/input";
import { useUpdateNode, useDeleteNode } from "../../../hooks/use-mutations";

type GraphInspectorProps = {
  width: number;
  selectedNode?: Node;
  workspaceId: string;
  onResizeStart: (event: React.PointerEvent<HTMLDivElement>) => void;
};

export function GraphInspector({ width, selectedNode, workspaceId, onResizeStart }: GraphInspectorProps) {
  const data = selectedNode?.data as KnowledgeNodeData | undefined;

  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  const updateNodeMutation = useUpdateNode(workspaceId);
  const deleteNodeMutation = useDeleteNode(workspaceId);

  useEffect(() => {
    if (data) {
      setTitle(data.title || "");
      setDescription(data.description || "");
      setIsEditing(false);
      setError(null);
    }
  }, [selectedNode?.id, data?.title, data?.description]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedNode || !title.trim()) {
      setError("Title is required");
      return;
    }
    setError(null);
    try {
      await updateNodeMutation.mutateAsync({
        nodeId: selectedNode.id,
        label: title.trim(),
        attributes: {
          description: description.trim(),
          confidence: data?.confidence ?? 50,
          connections: data?.connections ?? 0,
          positionX: selectedNode.position.x,
          positionY: selectedNode.position.y
        },
        version: data?.version ?? 1
      });
      setIsEditing(false);
    } catch (err: any) {
      setError(err.message || "Failed to update node");
    }
  };

  const handleDelete = async () => {
    if (!selectedNode) return;
    try {
      await deleteNodeMutation.mutateAsync(selectedNode.id);
    } catch (err: any) {
      setError(err.message || "Failed to delete node");
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
            <h2 className="mt-1 text-lg font-semibold">Node details</h2>
          </div>
          <PanelRightClose size={18} className="text-muted-foreground" />
        </div>

        {data ? (
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
                    {data.type || "Concept"}
                  </span>
                  <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="flex items-center gap-1 text-xs text-amber-300 hover:underline"
                  >
                    <Edit3 size={13} /> Edit
                  </button>
                </div>
                <h3 className="mt-3 text-xl font-semibold text-slate-100">{data.title}</h3>
                <p className="mt-3 text-sm leading-6 text-slate-300">
                  {data.description || "No description provided."}
                </p>
              </div>
            ) : (
              <form onSubmit={handleSave} className="rounded-xl border border-cyan-400/30 bg-white/[0.05] p-4 space-y-3">
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
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="mt-1 h-9 text-sm"
                    maxLength={255}
                    required
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300">Description / Content</label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
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
                <p className="mt-2 text-xl font-semibold text-slate-100">{data.confidence}%</p>
                <p className="mt-0.5 text-xs text-muted-foreground">Confidence</p>
              </div>
              <div className="rounded-xl border border-white/10 bg-black/18 p-3">
                <GitBranch size={15} className="text-violet-200" />
                <p className="mt-2 text-xl font-semibold text-slate-100">{data.connections}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">Connections</p>
              </div>
            </div>

            <div className="rounded-xl border border-white/10 bg-white/[0.045] p-4">
              <p className="text-xs font-semibold text-muted-foreground uppercase">Metadata</p>
              <div className="mt-3 space-y-2 text-xs">
                <div className="flex justify-between text-muted-foreground">
                  <span>ID</span>
                  <span className="font-mono text-[11px] text-slate-300 truncate max-w-[160px]">{selectedNode?.id}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Position X</span>
                  <span className="font-mono text-slate-300">{Math.round(selectedNode?.position.x ?? 0)}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Position Y</span>
                  <span className="font-mono text-slate-300">{Math.round(selectedNode?.position.y ?? 0)}</span>
                </div>
              </div>

              <div className="mt-4 border-t border-white/10 pt-3">
                <Button
                  type="button"
                  variant="ghost"
                  onClick={handleDelete}
                  disabled={deleteNodeMutation.isPending}
                  className="w-full h-8 justify-center gap-2 text-xs font-semibold text-rose-300 hover:bg-rose-500/10 hover:text-rose-200"
                >
                  {deleteNodeMutation.isPending ? <Loader2 size={13} className="animate-spin" /> : <Trash2 size={13} />}
                  Delete Node
                </Button>
              </div>
            </div>
          </div>
        ) : (
          <div className="mt-6 rounded-xl border border-white/10 bg-white/[0.04] p-4">
            <Info size={18} className="text-cyan-200" />
            <h3 className="mt-4 font-semibold text-slate-100">Select a node</h3>
            <p className="mt-2 text-sm leading-6 text-muted-foreground">
              Click any graph node to inspect or edit its title, content, positions, and parameters.
            </p>
          </div>
        )}
      </div>
    </motion.aside>
  );
}
