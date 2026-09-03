import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "../components/ui/toast";
import { authApi, notificationApi, workspaceApi, graphApi, nodeApi, edgeApi, socialApi, userApi, versioningApi } from "../services";
import type { WorkspaceNodesQuery } from "./use-queries";

export function useAuthMutation() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return {
    login: useMutation({
      mutationFn: (creds: { email: string; password: string }) => authApi.login(creds),
      onSuccess: (data) => {
        localStorage.setItem("auth_token", data.data.accessToken);
      },
      onError: (error: any) => {
        addToast({ type: "error", title: "Login failed", description: error.message });
      }
    }),
    register: useMutation({
      mutationFn: (data: { email: string; password: string; firstName: string; lastName: string }) => authApi.register(data),
      onSuccess: () => {
        addToast({ type: "info", title: "Verification code sent", description: "Please check your email address." });
      },
      onError: (error: any) => {
        addToast({ type: "error", title: "Registration failed", description: error.message });
      }
    }),
    logout: useMutation({
      mutationFn: () => authApi.logout(),
      onSuccess: () => {
        queryClient.clear();
        localStorage.removeItem("auth_token");
        localStorage.removeItem("auth_user");
        window.location.href = "/login";
      },
      onError: () => {
        localStorage.removeItem("auth_token");
        localStorage.removeItem("auth_user");
        window.location.href = "/login";
      }
    })
  };
}

export function useCreateNode(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: {
      nodeTypeId?: string;
      label: string;
      attributes: Record<string, unknown>;
      positionX?: number;
      positionY?: number;
    }) =>
      nodeApi.create(workspaceId, req.nodeTypeId, req.label, req.attributes, req.positionX, req.positionY),
    onSuccess: (response) => {
      queryClient.setQueryData<WorkspaceNodesQuery>(["workspace-nodes", workspaceId], (current) => {
        if (!current) return { nodes: [response.data], total: 1 };
        return {
          ...current,
          nodes: [...current.nodes.filter((n) => n.id !== response.data.id), response.data],
          total: (current.total || current.nodes.length) + 1
        };
      });
      queryClient.invalidateQueries({ queryKey: ["workspace-nodes", workspaceId] });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to create node", description: error.message });
    }
  });
}

export function useUpdateNode(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: {
      nodeId: string;
      label?: string;
      attributes?: Record<string, unknown>;
      version: number;
      positionX?: number;
      positionY?: number;
    }) =>
      nodeApi.update(
        workspaceId,
        req.nodeId,
        req.label || "",
        req.attributes || {},
        req.version,
        req.positionX,
        req.positionY
      ),
    onMutate: async (request) => {
      await queryClient.cancelQueries({ queryKey: ["workspace-nodes", workspaceId] });
      const queryKey = ["workspace-nodes", workspaceId] as const;
      const previous = queryClient.getQueryData<WorkspaceNodesQuery>(queryKey);
      queryClient.setQueryData<WorkspaceNodesQuery>(queryKey, (current) => {
        if (!current) return current;
        return {
          ...current,
          nodes: current.nodes.map((node) =>
            node.id === request.nodeId
              ? {
                  ...node,
                  label: request.label !== undefined && request.label !== "" ? request.label : node.label,
                  positionX: request.positionX !== undefined ? request.positionX : node.positionX,
                  positionY: request.positionY !== undefined ? request.positionY : node.positionY,
                  attributes: request.attributes ? { ...node.attributes, ...request.attributes } : node.attributes,
                  version: (node.version ?? 0) + 1
                }
              : node
          )
        };
      });
      return { previous };
    },
    onSuccess: (response) => {
      queryClient.setQueryData<WorkspaceNodesQuery>(["workspace-nodes", workspaceId], (current) => {
        if (!current) return current;
        return {
          ...current,
          nodes: current.nodes.map((node) => (node.id === response.data.id ? response.data : node))
        };
      });
    },
    onError: (error: any, _request, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["workspace-nodes", workspaceId], context.previous);
      }
      if (error?.status === 409 || error?.code === "CONFLICT") {
        addToast({ type: "error", title: "Version conflict", description: "This item was changed elsewhere. Refresh the latest version and try again." });
      } else {
        addToast({ type: "error", title: "Failed to update node", description: error.message });
      }
    }
  });
}

export function useDeleteNode(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (nodeId: string) => nodeApi.delete(workspaceId, nodeId),
    onSuccess: () => {
      addToast({ type: "success", title: "Node deleted", description: "The node has been removed." });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["workspace-nodes", workspaceId] });
    },
      onError: (error: any) => {
        addToast({ type: "error", title: "Failed to delete node", description: error.message });
    }
  });
}

export function useCreateEdge(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: {
      sourceNodeId: string;
      targetNodeId: string;
      relationshipType?: string;
      edgeTypeId?: string;
      label?: string;
      description?: string;
      weight?: number;
      attributes?: Record<string, unknown>;
    }) => edgeApi.create(workspaceId, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspace-edges", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
      addToast({ type: "success", title: "Connection established", description: "Relationship saved to graph." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Connection failed", description: error.message || "Failed to create relationship." });
    }
  });
}

export function useUpdateEdge(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: {
      edgeId: string;
      relationshipType?: string;
      edgeTypeId?: string;
      label?: string;
      description?: string;
      weight?: number;
      attributes?: Record<string, unknown>;
      version: number;
    }) => edgeApi.update(req.edgeId, {
      relationshipType: req.relationshipType,
      edgeTypeId: req.edgeTypeId,
      label: req.label,
      description: req.description,
      weight: req.weight,
      attributes: req.attributes,
      version: req.version
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspace-edges", workspaceId] });
      addToast({ type: "success", title: "Connection updated", description: "Relationship updated." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to update edge", description: error.message });
    }
  });
}

export function useDeleteEdge(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (edgeId: string) => edgeApi.delete(edgeId),
    onSuccess: () => {
      addToast({ type: "success", title: "Connection removed", description: "The relationship edge has been removed." });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["workspace-edges", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to delete edge", description: error.message });
    }
  });
}

export function useCreatePost(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { content: string; entityType: string; entityId: string }) =>
      socialApi.createPost(workspaceId, req.content, req.entityType, req.entityId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
      addToast({ type: "success", title: "Post created", description: "Your post has been published." });
      },
      onError: (error: any) => {
        addToast({ type: "error", title: "Failed to create post", description: error.message });
    }
  });
}

export function useLikePost(workspaceId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: string) => socialApi.likePost(workspaceId, postId),
    onMutate: async (postId: string) => {
      await queryClient.cancelQueries({ queryKey: ["social-posts", workspaceId] });
      const previous = queryClient.getQueryData(["social-posts", workspaceId]);
      queryClient.setQueryData(["social-posts", workspaceId], (old: any) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page: any) => ({
            ...page,
            content: page.content.map((post: any) =>
              post.id === postId
                ? { ...post, isLikedByCurrentUser: true, likeCount: (post.likeCount ?? 0) + 1 }
                : post
            )
          }))
        };
      });
      return { previous };
    },
    onError: (_err, _postId, context: any) => {
      if (context?.previous) {
        queryClient.setQueryData(["social-posts", workspaceId], context.previous);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
    }
  });
}

export function useUnlikePost(workspaceId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: string) => socialApi.unlikePost(workspaceId, postId),
    onMutate: async (postId: string) => {
      await queryClient.cancelQueries({ queryKey: ["social-posts", workspaceId] });
      const previous = queryClient.getQueryData(["social-posts", workspaceId]);
      queryClient.setQueryData(["social-posts", workspaceId], (old: any) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page: any) => ({
            ...page,
            content: page.content.map((post: any) =>
              post.id === postId
                ? { ...post, isLikedByCurrentUser: false, likeCount: Math.max(0, (post.likeCount ?? 0) - 1) }
                : post
            )
          }))
        };
      });
      return { previous };
    },
    onError: (_err, _postId, context: any) => {
      if (context?.previous) {
        queryClient.setQueryData(["social-posts", workspaceId], context.previous);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
    }
  });
}

export function useCreateComment(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { postId: string; content: string; parentCommentId?: string }) =>
      socialApi.addComment(req.postId, req.content, req.parentCommentId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["social-comments", workspaceId, variables.postId] });
    },
      onError: (error: any) => {
        addToast({ type: "error", title: "Failed to add comment", description: error.message });
      }
  });
}

export function useFavoritePost(workspaceId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: string) => socialApi.favoritePost(workspaceId, postId),
    onMutate: async (postId: string) => {
      await queryClient.cancelQueries({ queryKey: ["social-posts", workspaceId] });
      const previous = queryClient.getQueryData(["social-posts", workspaceId]);
      queryClient.setQueryData(["social-posts", workspaceId], (old: any) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page: any) => ({
            ...page,
            content: page.content.map((post: any) =>
              post.id === postId
                ? { ...post, isFavoritedByCurrentUser: true, favoriteCount: (post.favoriteCount ?? 0) + 1 }
                : post
            )
          }))
        };
      });
      return { previous };
    },
    onError: (_err, _postId, context: any) => {
      if (context?.previous) {
        queryClient.setQueryData(["social-posts", workspaceId], context.previous);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
    }
  });
}

export function useUnfavoritePost(workspaceId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (postId: string) => socialApi.unfavoritePost(workspaceId, postId),
    onMutate: async (postId: string) => {
      await queryClient.cancelQueries({ queryKey: ["social-posts", workspaceId] });
      const previous = queryClient.getQueryData(["social-posts", workspaceId]);
      queryClient.setQueryData(["social-posts", workspaceId], (old: any) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page: any) => ({
            ...page,
            content: page.content.map((post: any) =>
              post.id === postId
                ? { ...post, isFavoritedByCurrentUser: false, favoriteCount: Math.max(0, (post.favoriteCount ?? 0) - 1) }
                : post
            )
          }))
        };
      });
      return { previous };
    },
    onError: (_err, _postId, context: any) => {
      if (context?.previous) {
        queryClient.setQueryData(["social-posts", workspaceId], context.previous);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["social-posts", workspaceId] });
    }
  });
}

export function useFollowUser(workspaceId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (followeeId: string) => userApi.followUser(followeeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-follows", workspaceId] });
    }
  });
}

export function useCreateWorkspace() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { name: string; description: string; visibility?: 'PUBLIC' | 'PRIVATE' }) =>
      workspaceApi.create(req.name, req.description, req.visibility),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
      addToast({ type: "success", title: "Workspace created", description: "Your new workspace is ready." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to create workspace", description: error.message });
    }
  });
}

export function useCreateGraph() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE' }) =>
      graphApi.create(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      addToast({ type: "success", title: "Knowledge graph created", description: "Your new graph is ready." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to create graph", description: error.message });
    }
  });
}

export function useUpdateGraph() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { id: string; title: string; description?: string; visibility?: 'PUBLIC' | 'PRIVATE'; version?: number }) =>
      graphApi.update(req.id, { title: req.title, description: req.description, visibility: req.visibility, version: req.version }),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
      queryClient.invalidateQueries({ queryKey: ["graph", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      addToast({ type: "success", title: "Knowledge graph updated", description: "Changes saved successfully." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to update graph", description: error.message });
    }
  });
}

export function useDeleteGraph() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (id: string) => graphApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["graphs"] });
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      addToast({ type: "success", title: "Knowledge graph deleted", description: "The graph has been deleted." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to delete graph", description: error.message });
    }
  });
}

export function useCreateSnapshot(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { label: string; description: string }) =>
      versioningApi.createSnapshot(workspaceId, req.label, req.description),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["version-history", workspaceId] });
      addToast({ type: "success", title: "Snapshot created", description: "A new snapshot has been saved." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to create snapshot", description: error.message });
    }
  });
}

export function useForkGraph(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (req: { sourceVersionId: string; name: string; description: string }) =>
      versioningApi.createFork(workspaceId, req.sourceVersionId, req.name, req.description),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      addToast({ type: "success", title: "Graph forked", description: "A new forked workspace has been created." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to fork graph", description: error.message });
    }
  });
}

export function useRestoreVersion(workspaceId: string) {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (versionId: string) => versioningApi.restoreVersion(workspaceId, versionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["version-history", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["workspace-nodes", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["workspace-edges", workspaceId] });
      addToast({ type: "success", title: "Version restored", description: "The workspace has been restored to the selected version." });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to restore version", description: error.message });
    }
  });
}

export function useMarkAsRead() {
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  return useMutation({
    mutationFn: (id: string) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notifications-unread-count"] });
    },
    onError: (error: any) => {
      addToast({ type: "error", title: "Failed to mark as read", description: error.message });
    }
  });
}
