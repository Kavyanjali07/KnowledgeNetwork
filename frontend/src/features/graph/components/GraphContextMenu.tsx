import { motion } from "framer-motion";
import { FilePlus2, ScanSearch, Trash2 } from "lucide-react";

type GraphContextMenuProps = {
  x: number;
  y: number;
  onAddNode: () => void;
  onSearch: () => void;
  onClearSelection: () => void;
  onClose: () => void;
};

const menuItems = [
  { label: "Create node here", icon: FilePlus2 },
  { label: "Find related nodes", icon: ScanSearch },
  { label: "Clear selection", icon: Trash2, muted: true }
];

export function GraphContextMenu({ x, y, onAddNode, onSearch, onClearSelection, onClose }: GraphContextMenuProps) {
  return (
    <motion.div
      className="fixed z-[80] w-60 overflow-hidden rounded-xl border border-white/10 bg-[rgba(8,12,20,0.92)] p-2 shadow-glass backdrop-blur-2xl"
      style={{ left: x, top: y }}
      initial={{ opacity: 0, scale: 0.97, y: -4 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.97, y: -4 }}
      transition={{ duration: 0.14 }}
    >
      {menuItems.map((item, index) => (
        <button
          key={item.label}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm text-slate-200 transition hover:bg-white/8"
          onClick={() => {
            if (index === 0) onAddNode();
            if (index === 1) onSearch();
            if (index === 2) onClearSelection();
            onClose();
          }}
        >
          <item.icon size={15} className={item.muted ? "text-rose-200" : "text-cyan-200"} />
          {item.label}
        </button>
      ))}
    </motion.div>
  );
}
