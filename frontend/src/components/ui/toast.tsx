import { createContext, useContext, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { CheckCircle2, Info, Loader2, XCircle } from "lucide-react";

type ToastType = "success" | "error" | "info" | "loading";

type ToastItem = {
  id: string;
  type: ToastType;
  title: string;
  description?: string;
};

type ToastContextValue = {
  toasts: ToastItem[];
  addToast: (toast: Omit<ToastItem, "id">) => void;
  removeToast: (id: string) => void;
};

const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error("useToast must be used inside ToastProvider");
  return context;
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const addToast = (toast: Omit<ToastItem, "id">) => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    setToasts((prev) => [...prev, { ...toast, id }]);
    if (toast.type !== "loading") {
      setTimeout(() => removeToast(id), 3500);
    }
  };

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  return (
    <ToastContext.Provider value={{ toasts, addToast, removeToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-[140] flex flex-col gap-2">
        <AnimatePresence>
          {toasts.map((toast) => (
            <motion.div
              key={toast.id}
              initial={{ opacity: 0, y: 12, scale: 0.96 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8, scale: 0.95 }}
              transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="flex w-80 flex-col gap-1 rounded-xl border border-white/12 bg-[rgba(8,12,20,0.96)] p-3 shadow-glass backdrop-blur-2xl"
            >
              <div className="flex items-center gap-2">
                {toast.type === "success" && <CheckCircle2 size={16} className="text-emerald-300" />}
                {toast.type === "error" && <XCircle size={16} className="text-rose-300" />}
                {toast.type === "info" && <Info size={16} className="text-cyan-300" />}
                {toast.type === "loading" && <Loader2 size={16} className="animate-spin text-cyan-300" />}
                <span className="text-sm font-medium text-slate-100">{toast.title}</span>
              </div>
              {toast.description && <p className="text-xs text-muted-foreground">{toast.description}</p>}
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
}
