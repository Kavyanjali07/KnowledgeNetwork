package com.knowledgenetwork.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.SocialComment;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.payload.request.SocialCommentRequest;
import com.knowledgenetwork.domain.payload.response.SocialCommentResponse;
import com.knowledgenetwork.domain.payload.response.SocialPostResponse;
import com.knowledgenetwork.domain.payload.response.SocialSummaryResponse;
import com.knowledgenetwork.domain.payload.response.SocialUserSummary;
import com.knowledgenetwork.service.SocialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/social")
@Tag(name = "Social", description = "Social interactions: posts, comments, likes, favorites, tags, and follows")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @PostMapping("/posts")
    @Operation(summary = "Create a new social post")
    public ResponseEntity<ApiResponse<SocialPostResponse>> createPost(
            @RequestParam UUID workspaceId,
            @RequestParam @NotBlank String content,
            @RequestParam String entityType,
            @RequestParam UUID entityId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        SocialPost post = socialService.createPost(workspaceId, content, entityType, entityId);
        SocialPostResponse response = mapToPostResponse(post, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", response));
    }

    @GetMapping("/posts/{workspaceId}")
    @Operation(summary = "List posts for a workspace")
    public ResponseEntity<ApiResponse<PageResponse<SocialPostResponse>>> getPosts(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SocialPost> posts = socialService.getPosts(workspaceId, pageable);
        UUID currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
        PageResponse<SocialPostResponse> pageResponse = PageResponse.from(
                posts.map(post -> mapToPostResponse(post, currentUserId)));
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", pageResponse));
    }

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.likePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", null));
    }

    @DeleteMapping("/posts/{postId}/unlike")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.unlikePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", null));
    }

    @PostMapping("/posts/{postId}/favorite")
    @Operation(summary = "Favorite a post")
    public ResponseEntity<ApiResponse<Void>> favoritePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.favoritePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post favorited successfully", null));
    }

    @DeleteMapping("/posts/{postId}/unfavorite")
    @Operation(summary = "Unfavorite a post")
    public ResponseEntity<ApiResponse<Void>> unfavoritePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.unfavoritePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post unfavorited successfully", null));
    }

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<ApiResponse<SocialCommentResponse>> addComment(
            @PathVariable UUID postId,
            @RequestBody SocialCommentRequest request,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        SocialComment comment = socialService.addComment(postId, request.getContent(), request.getParentCommentId());
        SocialCommentResponse response = mapToCommentResponse(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "List comments for a post")
    public ResponseEntity<ApiResponse<PageResponse<SocialCommentResponse>>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SocialComment> comments = socialService.getComments(postId, pageable);
        PageResponse<SocialCommentResponse> pageResponse = PageResponse.from(
                comments.map(this::mapToCommentResponse));
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", pageResponse));
    }

    @PostMapping("/posts/{postId}/tags")
    @Operation(summary = "Add a tag to a post")
    public ResponseEntity<ApiResponse<Void>> addTag(
            @PathVariable UUID postId,
            @RequestParam @NotBlank String tag,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.addTag(postId, tag);
        return ResponseEntity.ok(ApiResponse.success("Tag added successfully", null));
    }

    @PostMapping("/follow")
    @Operation(summary = "Follow a user")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @RequestParam UUID followeeId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.followUser(followeeId);
        return ResponseEntity.ok(ApiResponse.success("User followed successfully", null));
    }

    @DeleteMapping("/follow")
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @RequestParam UUID followeeId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        socialService.unfollowUser(followeeId);
        return ResponseEntity.ok(ApiResponse.success("User unfollowed successfully", null));
    }

    @GetMapping("/follow/status")
    @Operation(summary = "Check if the current user follows another user")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @RequestParam UUID followeeId,
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success("Follow status retrieved successfully", socialService.isFollowing(followeeId)));
    }

    @GetMapping("/posts/{postId}/summary")
    @Operation(summary = "Get social summary for a post")
    public ResponseEntity<ApiResponse<SocialSummaryResponse>> getSummary(
            @PathVariable UUID postId) {
        Map<String, Object> summary = socialService.getSocialSummary(postId);
        SocialSummaryResponse response = new SocialSummaryResponse();
        response.setPostId(UUID.fromString(summary.get("postId").toString()));
        response.setLikes(((Number) summary.get("likes")).longValue());
        response.setFavorites(((Number) summary.get("favorites")).longValue());
        response.setComments(((Number) summary.get("comments")).longValue());
        response.setTags((List<String>) summary.get("tags"));
        return ResponseEntity.ok(ApiResponse.success("Summary retrieved successfully", response));
    }

    private SocialPostResponse mapToPostResponse(SocialPost post, UUID currentUserId) {
        SocialPostResponse response = new SocialPostResponse();
        response.setId(post.getId());
        response.setWorkspaceId(post.getWorkspace().getId());
        response.setAuthor(mapToUserSummary(post.getAuthor()));
        response.setContent(post.getContent());
        response.setEntityType(post.getEntityType());
        response.setEntityId(post.getEntityId());
        response.setLikeCount(socialService.getLikeCount(post.getId()));
        response.setFavoriteCount(socialService.getFavoriteCount(post.getId()));
        response.setCommentCount(socialService.getCommentCount(post.getId()));
        response.setTags(socialService.getTags(post.getId()));
        if (currentUserId != null) {
            response.setLikedByCurrentUser(socialService.isLikedByUser(post.getId(), currentUserId));
            response.setFavoritedByCurrentUser(socialService.isFavoritedByUser(post.getId(), currentUserId));
        }
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setCreatedBy(post.getCreatedBy());
        response.setUpdatedBy(post.getUpdatedBy());
        response.setVersion(post.getVersion());
        response.setDeleted(post.isDeleted());
        return response;
    }

    private SocialCommentResponse mapToCommentResponse(SocialComment comment) {
        SocialCommentResponse response = new SocialCommentResponse();
        response.setId(comment.getId());
        response.setWorkspaceId(comment.getWorkspace().getId());
        response.setPostId(comment.getPost().getId());
        response.setAuthor(mapToUserSummary(comment.getAuthor()));
        response.setContent(comment.getContent());
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setCreatedBy(comment.getCreatedBy());
        response.setUpdatedBy(comment.getUpdatedBy());
        response.setVersion(comment.getVersion());
        response.setDeleted(comment.isDeleted());
        return response;
    }

    private SocialUserSummary mapToUserSummary(com.knowledgenetwork.domain.model.User user) {
        SocialUserSummary summary = new SocialUserSummary();
        summary.setId(user.getId());
        summary.setEmail(user.getEmail());
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        return summary;
    }
}
