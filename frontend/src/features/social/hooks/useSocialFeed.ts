import { useCallback } from "react";
import { useInfiniteQuery } from "@tanstack/react-query";
import { socialApi } from "../../../services";
import { useCreatePost, useLikePost, useUnlikePost, useCreateComment, useFavoritePost, useUnfavoritePost } from "../../../hooks/use-mutations";
import { useCurrentWorkspace } from "../../../hooks/use-queries";
import type { SocialPost, ReactionEmoji } from "../types";
import type { SocialPostResponse } from "../../../services";

function mapPostResponse(post: SocialPostResponse): SocialPost {
  return {
    id: post.id,
    workspaceId: post.workspaceId,
    authorId: post.authorId,
    authorName:
      post.author.firstName || post.author.lastName
        ? `${post.author.firstName} ${post.author.lastName}`
        : post.author.email,
    authorUsername: post.author.email.split("@")[0] ?? post.authorId,
    content: post.content,
    entityType: post.entityType,
    entityName: post.entityType,
    createdAt: post.createdAt,
    likes: post.likeCount,
    likedByCurrentUser: post.isLikedByCurrentUser,
    reactions: [],
    comments: [],
    favorites: post.favoriteCount,
    favoritedByCurrentUser: post.isFavoritedByCurrentUser,
    commentCount: post.commentCount
  };
}

export function useSocialFeed() {
  const { workspaceId, isLoading: isWorkspaceLoading, isError: isWorkspaceError, error: workspaceError } = useCurrentWorkspace();
  const effectiveWorkspaceId = workspaceId ?? "";

  const {
    data,
    isLoading,
    isError,
    error,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    refetch
  } = useInfiniteQuery({
    queryKey: ["social-posts", effectiveWorkspaceId],
    queryFn: ({ pageParam = 0 }) =>
      socialApi.getPosts(effectiveWorkspaceId, pageParam, 20).then((r) => r.data),
    getNextPageParam: (lastPage) => {
      if (lastPage.pageNumber < lastPage.totalPages - 1) {
        return lastPage.pageNumber + 1;
      }
      return undefined;
    },
    initialPageParam: 0,
    enabled: Boolean(effectiveWorkspaceId)
  });

  const posts: SocialPost[] =
    data?.pages.flatMap((page) => page.content.map(mapPostResponse)) ?? [];
  const hasMore = hasNextPage ?? false;

  const likeMutation = useLikePost(effectiveWorkspaceId);
  const unlikeMutation = useUnlikePost(effectiveWorkspaceId);
  const favoriteMutation = useFavoritePost(effectiveWorkspaceId);
  const unfavoriteMutation = useUnfavoritePost(effectiveWorkspaceId);
  const createPostMutation = useCreatePost(effectiveWorkspaceId);
  const createCommentMutation = useCreateComment(effectiveWorkspaceId);

  const togglePostLike = useCallback(
    (postId: string) => {
      const post = posts.find((p) => p.id === postId);
      if (!post) return;
      if (post.likedByCurrentUser) {
        unlikeMutation.mutate(postId);
      } else {
        likeMutation.mutate(postId);
      }
    },
    [posts, likeMutation, unlikeMutation]
  );

  const togglePostReaction = useCallback(
    (_postId: string, _emoji: ReactionEmoji) => {
    },
    []
  );

  const togglePostFavorite = useCallback(
    (postId: string) => {
      const post = posts.find((item) => item.id === postId);
      if (!post) return;
      if (post.favoritedByCurrentUser) {
        unfavoriteMutation.mutate(postId);
      } else {
        favoriteMutation.mutate(postId);
      }
    },
    [favoriteMutation, posts, unfavoriteMutation]
  );

  const addComment = useCallback(
    (postId: string, content: string, parentId?: string) => {
      createCommentMutation.mutate({
        postId,
        content,
        parentCommentId: parentId
      });
    },
    [createCommentMutation]
  );

  const toggleCommentLike = useCallback(
    (_postId: string, _commentId: string) => {
    },
    []
  );

  const toggleCommentReaction = useCallback(
    (_postId: string, _commentId: string, _emoji: ReactionEmoji) => {
    },
    []
  );

  const addPost = useCallback(
    (content: string) => {
      if (!effectiveWorkspaceId) return;
      createPostMutation.mutate({
        content,
        entityType: "Graph",
        entityId: effectiveWorkspaceId
      });
    },
    [createPostMutation, effectiveWorkspaceId]
  );

  const loadMore = useCallback(() => {
    if (hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  return {
    posts,
    workspaceId: effectiveWorkspaceId || null,
    hasMore,
    isLoading: isWorkspaceLoading || isLoading,
    initialLoad: isWorkspaceLoading || isLoading,
    loadMore,
    togglePostLike,
    togglePostFavorite,
    togglePostReaction,
    addComment,
    toggleCommentLike,
    toggleCommentReaction,
    addPost,
    isError: isWorkspaceError || isError,
    error: workspaceError ?? error,
    refetch,
    isFetchingNextPage
  };
}
