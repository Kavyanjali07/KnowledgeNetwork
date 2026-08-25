import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { motion } from "framer-motion";
import { GraphEditor } from "../components/GraphEditor";
import { useCurrentWorkspace } from "../../../hooks/use-queries";

export function GraphEditorPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { id } = useParams<{ id?: string }>();
  const { workspaceId: currentWorkspaceId, isLoading: workspaceLoading } = useCurrentWorkspace();

  const workspaceId = id || searchParams.get("workspaceId") || currentWorkspaceId;

  if (workspaceLoading && !workspaceId) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-300 border-t-transparent" />
      </div>
    );
  }

  if (!workspaceId) {
    navigate("/graphs", { replace: true });
    return null;
  }

  return (
    <section className="mx-auto mt-6 max-w-[1600px] pt-4">
      <motion.div
        className="mb-5 flex flex-wrap items-end justify-between gap-4"
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.28, ease: [0.16, 1, 0.3, 1] }}
      >
        <div>
          <p className="text-sm text-muted-foreground">Graph editor</p>
          <h1 className="mt-2 text-2xl font-semibold tracking-[-0.015em] md:text-4xl">Knowledge canvas</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
            Model concepts, artifacts, people, and decisions on an infinite canvas with live autosave and keyboard-first controls.
          </p>
        </div>
        <div className="rounded-full border border-emerald-300/20 bg-emerald-300/10 px-3 py-1 text-xs text-emerald-100">
          Autosave enabled
        </div>
      </motion.div>
      <GraphEditor workspaceId={workspaceId} />
    </section>
  );
}

