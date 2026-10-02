import { useState } from "react";

export interface KnowledgeTrailStep {
  id: string;
  type: "hub" | "concept" | "network" | "creator";
  label: string;
  url: string;
  timestamp: number;
}

const STORAGE_KEY = "kn_knowledge_trail";
const MAX_STEPS = 8;

export function useKnowledgeTrail() {
  const [trail, setTrail] = useState<KnowledgeTrailStep[]>(() => {
    try {
      const saved = sessionStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const addStep = (step: Omit<KnowledgeTrailStep, "id" | "timestamp">) => {
    setTrail((prev) => {
      // Don't add duplicate if the last step is identical URL
      if (prev.length > 0 && prev[prev.length - 1]?.url === step.url) {
        return prev;
      }


      const newStep: KnowledgeTrailStep = {
        ...step,
        id: Math.random().toString(36).substring(2, 9),
        timestamp: Date.now()
      };

      const updated = [...prev, newStep].slice(-MAX_STEPS);
      try {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      } catch {
        // ignore quota errors
      }
      return updated;
    });
  };

  const clearTrail = () => {
    setTrail([]);
    try {
      sessionStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
  };

  return { trail, addStep, clearTrail };
}
