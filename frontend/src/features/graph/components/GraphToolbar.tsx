import { Maximize2, MousePointer2, Network, Plus, Route, Save, Search, Trash2 } from "lucide-react";
import { Button } from "../../../components/ui/button";
import type { AutosaveState } from "../types";

type GraphToolbarProps = {
  autosaveState: AutosaveState;
  onAddNode: () => void;
  onConnect: () => void;
  onSelect: () => void;
  onFitView: () => void;
  onDeleteSelected: () => void;
  onSearch: () => void;
};

export function GraphToolbar({ autosaveState, onAddNode, onConnect, onSelect, onFitView, onDeleteSelected, onSearch }: GraphToolbarProps) {
  const autosaveLabel = autosaveState === "saving" ? "Saving" : autosaveState === "saved" ? "Saved" : "Idle";

  return (
    <div className="pointer-events-auto flex flex-wrap items-center gap-2 rounded-xl border border-white/10 bg-[rgba(8,12,20,0.78)] p-2 shadow-glass backdrop-blur-2xl">
      <Button variant="secondary" className="h-9 px-3" onClick={onAddNode} title="Create a new knowledge item (N)">
        <Plus size={15} />
        Node
      </Button>
      <Button variant="secondary" className="h-9 px-3" onClick={onConnect} title="Drag from one node handle to another">
        <Route size={15} />
        Connect
      </Button>
      <Button variant="ghost" className="h-9 px-3" onClick={onSelect} title="Select mode — click nodes to inspect">
        <MousePointer2 size={15} />
        Select
      </Button>
      <Button variant="ghost" className="h-9 px-3" onClick={onFitView} title="Center and fit all nodes (F)">
        <Maximize2 size={15} />
        Fit
      </Button>
      <Button variant="ghost" className="h-9 px-3" onClick={onDeleteSelected} title="Delete selected node or edge (Del)">
        <Trash2 size={15} />
      </Button>
      <div className="mx-1 hidden h-6 w-px bg-white/10 sm:block" />
      <Button variant="ghost" className="h-9 px-3" onClick={onSearch} title="Search nodes, edges, commands (⌘K)">
        <Search size={15} />
      </Button>
      <div className="flex items-center gap-2 rounded-lg border border-white/10 bg-white/[0.045] px-3 py-2 text-xs text-muted-foreground">
        <Save size={14} className={autosaveState === "saving" ? "animate-pulse text-cyan-200" : "text-emerald-200"} />
        {autosaveLabel}
      </div>
      <div className="hidden items-center gap-2 rounded-lg border border-cyan-300/20 bg-cyan-300/10 px-3 py-2 text-xs text-cyan-100 lg:flex">
        <Network size={14} />
        Infinite canvas
      </div>
    </div>
  );
}
