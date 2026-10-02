import { useState, useEffect } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { Lock, ArrowLeft, LogIn } from "lucide-react";
import { GraphEditor } from "../components/GraphEditor";
import { PublicNetworkHeader } from "../components/PublicNetworkHeader";
import { PublishNetworkModal } from "../components/PublishNetworkModal";
import { UnpublishNetworkModal } from "../components/UnpublishNetworkModal";
import { ProvenancePanel } from "../components/ProvenancePanel";
import { RelatedNetworksDrawer } from "../components/RelatedNetworksDrawer";
import { useKnowledgeTrail } from "../../explore/hooks/useKnowledgeTrail";
import { useGraph, useCurrentWorkspace } from "../../../hooks/use-queries";
import { usePublishGraph, useUnpublishGraph, useForkGraph } from "../../../hooks/use-mutations";
import { useAuth } from "../../../lib/auth-context";
import { Button } from "../../../components/ui/button";
import { useToast } from "../../../components/ui/toast";
import type { LicenseType } from "../../../services/graphApi";

export function GraphEditorPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { id } = useParams<{ id?: string }>();
  const { currentUser } = useAuth();

  const { addToast } = useToast();
  const { addStep } = useKnowledgeTrail();


  const { workspaceId: currentWorkspaceId, isLoading: workspaceLoading } = useCurrentWorkspace();

  const workspaceId = id || searchParams.get("workspaceId") || currentWorkspaceId;
  const fromConcept = searchParams.get("fromConcept") || undefined;

  // Modal visibility states
  const [isPublishOpen, setIsPublishOpen] = useState(false);
  const [isUnpublishOpen, setIsUnpublishOpen] = useState(false);
  const [isProvenanceOpen, setIsProvenanceOpen] = useState(false);
  const [isRelatedOpen, setIsRelatedOpen] = useState(false);

  // Queries & Mutations
  const { data: graph, isLoading: isGraphLoading, isError: isGraphError, error: graphError } = useGraph(workspaceId || "");
  const publishMutation = usePublishGraph();
  const unpublishMutation = useUnpublishGraph();
  const forkMutation = useForkGraph(workspaceId || "");

  useEffect(() => {
    if (graph && workspaceId) {
      addStep({
        type: "network",
        label: graph.title || graph.name || "Knowledge Network",
        url: `/graphs/${workspaceId}${fromConcept ? `?fromConcept=${encodeURIComponent(fromConcept)}` : ""}`
      });
    }
  }, [graph?.id, workspaceId, fromConcept]);


  if ((workspaceLoading || isGraphLoading) && !graph) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-cyan-300 border-t-transparent" />
        <p className="text-xs text-muted-foreground">Loading Knowledge Network...</p>
      </div>
    );
  }

  // Handle Unauthorized / Private Network Access Denied (RFC 7807 Error)
  if (isGraphError || !graph) {
    const errorObj = (graphError as any) || {};
    const status = errorObj.status || 403;
    const detail = errorObj.message || "This Knowledge Network is private. Access is restricted to the network owner and authorized members.";

    return (
      <div className="mx-auto mt-16 max-w-lg rounded-2xl border border-rose-500/30 bg-[rgba(16,10,12,0.95)] p-8 text-center shadow-2xl backdrop-blur-2xl">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 mb-4">
          <Lock size={28} />
        </div>

        <span className="rounded-full border border-rose-500/30 bg-rose-500/10 px-3 py-1 text-[11px] font-semibold text-rose-300">
          HTTP {status} — ACCESS RESTRICTED
        </span>

        <h2 className="mt-3 text-2xl font-bold text-slate-100">Private Knowledge Network</h2>
        <p className="mt-2 text-xs leading-relaxed text-slate-300">{detail}</p>

        <div className="mt-6 flex flex-wrap items-center justify-center gap-3 border-t border-white/10 pt-5">
          <Button
            variant="secondary"
            onClick={() => navigate("/graphs")}
            className="border-white/10 bg-white/5 text-xs text-slate-200 hover:bg-white/10 gap-2"
          >

            <ArrowLeft size={14} />
            Explore Knowledge Library
          </Button>

          {!currentUser && (
            <Button
              onClick={() => navigate("/login")}
              className="bg-cyan-500 text-slate-950 text-xs font-semibold hover:bg-cyan-400 gap-2"
            >
              <LogIn size={14} />
              Sign In
            </Button>
          )}
        </div>
      </div>
    );
  }

  const isOwnerOrEditor = Boolean(currentUser && graph.ownerId === currentUser.id);

  const isReadOnly = !isOwnerOrEditor;

  const handlePublish = async (licenseType: LicenseType, customAttribution?: string) => {
    await publishMutation.mutateAsync({
      id: graph.id,
      licenseType,
      customAttribution
    });
  };

  const handleUnpublish = async () => {
    await unpublishMutation.mutateAsync(graph.id);
  };

  const handleFork = async () => {
    try {
      await forkMutation.mutateAsync({
        sourceVersionId: graph.id,
        name: `Derived Work of ${graph.title}`,
        description: `Forked derivative knowledge network from ${graph.title} by ${graph.ownerName || "Creator"}.`
      });
      addToast({ type: "success", title: "Network Derived", description: "Successfully created derived knowledge network!" });
    } catch (err: any) {
      addToast({ type: "error", title: "Derivation Failed", description: err.message || "Failed to fork network." });
    }
  };

  return (
    <section className="mx-auto mt-4 max-w-[1600px] px-4 pt-2">
      <PublicNetworkHeader
        graph={graph}
        isOwnerOrEditor={isOwnerOrEditor}
        isReadOnly={isReadOnly}
        fromConcept={fromConcept}
        onOpenProvenance={() => setIsProvenanceOpen(true)}
        onOpenRelated={() => setIsRelatedOpen(true)}
        onFork={handleFork}
        onOpenPublishModal={() => setIsPublishOpen(true)}
        onOpenUnpublishModal={() => setIsUnpublishOpen(true)}
      />

      <GraphEditor workspaceId={workspaceId || ""} isReadOnly={isReadOnly} />

      {/* Confirmation, Provenance & Related Networks Drawers/Modals */}
      <PublishNetworkModal
        isOpen={isPublishOpen}
        onClose={() => setIsPublishOpen(false)}
        graph={graph as any}
        onPublish={handlePublish}
        isPending={publishMutation.isPending}
      />

      <UnpublishNetworkModal
        isOpen={isUnpublishOpen}
        onClose={() => setIsUnpublishOpen(false)}
        graph={graph as any}
        onUnpublish={handleUnpublish}
        isPending={unpublishMutation.isPending}
      />

      <ProvenancePanel
        isOpen={isProvenanceOpen}
        onClose={() => setIsProvenanceOpen(false)}
        workspaceId={workspaceId || ""}
        networkTitle={graph.title || graph.name}
        ownerName={graph.ownerName}
      />

      <RelatedNetworksDrawer
        isOpen={isRelatedOpen}
        onClose={() => setIsRelatedOpen(false)}
        networkId={workspaceId || ""}
        networkTitle={graph.title || graph.name}
      />
    </section>
  );
}

