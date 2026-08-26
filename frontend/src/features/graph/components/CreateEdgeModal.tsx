import React, { useState } from "react";
import { Link2, X } from "lucide-react";

export interface CreateEdgeModalProps {
  isOpen: boolean;
  onClose: () => void;
  sourceNodeTitle: string;
  targetNodeTitle: string;
  onSubmit: (data: { relationshipType: string; label: string; description: string }) => void;
  isLoading?: boolean;
}

const RELATIONSHIP_TYPES = [
  { value: "RELATED_TO", label: "Related to", description: "General association between ideas" },
  { value: "DEPENDS_ON", label: "Depends on", description: "Source relies on target" },
  { value: "PART_OF", label: "Part of", description: "Source is a component of target" },
  { value: "PREREQUISITE_OF", label: "Prerequisite of", description: "Source must precede target" },
  { value: "CAUSES", label: "Causes", description: "Source leads to or produces target" },
  { value: "SUPPORTS", label: "Supports", description: "Source provides evidence for target" },
  { value: "CONTRADICTS", label: "Contradicts", description: "Source conflicts with target" },
  { value: "REFERENCES", label: "References", description: "Source cites or mentions target" }
];

export const CreateEdgeModal: React.FC<CreateEdgeModalProps> = ({
  isOpen,
  onClose,
  sourceNodeTitle,
  targetNodeTitle,
  onSubmit,
  isLoading = false
}) => {
  const [relationshipType, setRelationshipType] = useState<string>("RELATED_TO");
  const [label, setLabel] = useState<string>("");
  const [description, setDescription] = useState<string>("");

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      relationshipType,
      label: label.trim(),
      description: description.trim()
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-md animate-in fade-in duration-200">
      <div className="relative w-full max-w-lg overflow-hidden rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/50">
          <div className="flex items-center gap-2 text-cyan-400">
            <Link2 className="w-5 h-5" />
            <h2 className="text-lg font-semibold text-white">Connect Ideas</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1 text-slate-400 rounded-lg hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Connection Preview Badge */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-slate-950/60 border border-slate-800 text-sm">
            <span className="font-medium text-cyan-300 truncate max-w-[180px]" title={sourceNodeTitle}>
              {sourceNodeTitle || "Source Node"}
            </span>
            <span className="text-xs px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 font-mono">
              &rarr;
            </span>
            <span className="font-medium text-cyan-300 truncate max-w-[180px]" title={targetNodeTitle}>
              {targetNodeTitle || "Target Node"}
            </span>
          </div>

          {/* Relationship Type Selection */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
              Relationship Type <span className="text-rose-500">*</span>
            </label>
            <select
              value={relationshipType}
              onChange={(e) => setRelationshipType(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white focus:outline-none focus:border-cyan-500 transition-colors text-sm"
            >
              {RELATIONSHIP_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label} ({t.description})
                </option>
              ))}
            </select>
          </div>

          {/* Custom Label (Optional) */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
              Connection Label <span className="text-slate-500">(Optional)</span>
            </label>
            <input
              type="text"
              placeholder="e.g. requires approval, builds on..."
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 transition-colors text-sm"
              maxLength={255}
            />
          </div>

          {/* Description (Optional) */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-400">
              Description / Rationale <span className="text-slate-500">(Optional)</span>
            </label>
            <textarea
              placeholder="Explain the relationship between these nodes..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 transition-colors text-sm resize-none"
              maxLength={2000}
            />
          </div>

          {/* Footer Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-slate-400 rounded-xl hover:text-white hover:bg-slate-800 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-5 py-2 text-sm font-medium text-slate-950 bg-cyan-400 hover:bg-cyan-300 rounded-xl shadow-lg shadow-cyan-500/20 disabled:opacity-50 transition-all"
            >
              {isLoading ? "Connecting..." : "Create Connection"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
