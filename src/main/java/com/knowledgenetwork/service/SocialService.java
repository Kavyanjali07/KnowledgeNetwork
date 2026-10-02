package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.SocialComment;
import com.knowledgenetwork.domain.model.SocialFavorite;
import com.knowledgenetwork.domain.model.SocialFollow;
import com.knowledgenetwork.domain.model.SocialLike;
import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.SocialTag;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.SocialCommentRepository;
import com.knowledgenetwork.repository.SocialFavoriteRepository;
import com.knowledgenetwork.repository.SocialFollowRepository;
import com.knowledgenetwork.repository.SocialLikeRepository;
import com.knowledgenetwork.repository.SocialPostRepository;
import com.knowledgenetwork.repository.SocialTagRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class SocialService {

    private final WorkspaceRepository workspaceRepository;
    private final SocialPostRepository socialPostRepository;
    private final SocialLikeRepository socialLikeRepository;
    private final SocialFavoriteRepository socialFavoriteRepository;
    private final SocialCommentRepository socialCommentRepository;
    private final SocialTagRepository socialTagRepository;
    private final SocialFollowRepository socialFollowRepository;
    private final UserRepository userRepository;
    private final WorkspaceSecurityValidator workspaceSecurityValidator;
    private final NotificationService notificationService;

    public SocialService(WorkspaceRepository workspaceRepository,
                          SocialPostRepository socialPostRepository,
                          SocialLikeRepository socialLikeRepository,
                          SocialFavoriteRepository socialFavoriteRepository,
                          SocialCommentRepository socialCommentRepository,
                          SocialTagRepository socialTagRepository,
                          SocialFollowRepository socialFollowRepository,
                          UserRepository userRepository,
                          WorkspaceSecurityValidator workspaceSecurityValidator,
                          NotificationService notificationService) {
        this.workspaceRepository = workspaceRepository;
        this.socialPostRepository = socialPostRepository;
        this.socialLikeRepository = socialLikeRepository;
        this.socialFavoriteRepository = socialFavoriteRepository;
        this.socialCommentRepository = socialCommentRepository;
        this.socialTagRepository = socialTagRepository;
        this.socialFollowRepository = socialFollowRepository;
        this.userRepository = userRepository;
        this.workspaceSecurityValidator = workspaceSecurityValidator;
        this.notificationService = notificationService;
    }

    @Transactional
    public SocialPost createPost(UUID workspaceId, String content, String entityType, UUID entityId) {
        Workspace workspace = getWorkspace(workspaceId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);
        User author = getUser(currentUserId);
        SocialPost post = new SocialPost(workspace, author, content, entityType, entityId);
        String userId = currentUserId.toString();
        post.setCreatedBy(userId);
        post.setUpdatedBy(userId);
        return socialPostRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<SocialPost> getPosts(UUID workspaceId, Pageable pageable) {
        Workspace workspace = getWorkspace(workspaceId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        return socialPostRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable);
    }

    @Transactional
    public void likePost(UUID postId) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        User user = getUser(currentUserId);
        if (socialLikeRepository.existsByPostAndUser(post, user)) {
            throw new BusinessException("User already liked this post");
        }
        socialLikeRepository.save(new SocialLike(workspace, post, user));

        if (post.getAuthor() != null && !post.getAuthor().getId().equals(currentUserId)) {
            notificationService.createNotification(
                    workspace,
                    post.getAuthor(),
                    "LIKE",
                    user.getFirstName() + " " + user.getLastName() + " liked your post"
            );
        }
    }

    @Transactional
    public void unlikePost(UUID postId) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        User user = getUser(currentUserId);
        socialLikeRepository.findByPostAndUser(post, user).ifPresent(socialLikeRepository::delete);
    }

    @Transactional
    public void favoritePost(UUID postId) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        User user = getUser(currentUserId);
        if (socialFavoriteRepository.existsByPostAndUser(post, user)) {
            throw new BusinessException("User already favorited this post");
        }
        socialFavoriteRepository.save(new SocialFavorite(workspace, post, user));
    }

    @Transactional
    public void unfavoritePost(UUID postId) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateReadAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        User user = getUser(currentUserId);
        socialFavoriteRepository.findByPostAndUser(post, user).ifPresent(socialFavoriteRepository::delete);
    }

    @Transactional
    public SocialComment addComment(UUID postId, String content, UUID parentCommentId) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        User author = getUser(currentUserId);
        SocialComment comment = new SocialComment(workspace, post, author, content);
        if (parentCommentId != null) {
            SocialComment parent = socialCommentRepository.findByIdAndWorkspaceAndIsDeletedFalse(parentCommentId, workspace)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentCommentId));
            comment.setParentComment(parent);
        }
        String userId = currentUserId.toString();
        comment.setCreatedBy(userId);
        comment.setUpdatedBy(userId);
        SocialComment saved = socialCommentRepository.save(comment);

        if (post.getAuthor() != null && !post.getAuthor().getId().equals(currentUserId)) {
            notificationService.createNotification(
                    workspace,
                    post.getAuthor(),
                    "COMMENT",
                    author.getFirstName() + " " + author.getLastName() + " commented on your post"
            );
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<SocialComment> getComments(UUID postId, Pageable pageable) {
        Workspace workspace = getWorkspaceForPost(postId);
        workspaceSecurityValidator.validateReadAccess(workspace, SecurityUtils.getCurrentUserId());
        SocialPost post = getPost(postId, workspace);
        return socialCommentRepository.findByPostAndIsDeletedFalse(post, pageable);
    }

    @Transactional
    public void addTag(UUID postId, String tag) {
        Workspace workspace = getWorkspaceForPost(postId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        workspaceSecurityValidator.validateWriteAccess(workspace, currentUserId);
        SocialPost post = getPost(postId, workspace);
        if (socialTagRepository.existsByPostAndTag(post, tag.toLowerCase())) {
            throw new BusinessException("Tag already exists for this post");
        }
        socialTagRepository.save(new SocialTag(workspace, post, tag.toLowerCase()));
    }


    @Transactional
    public void followUser(UUID followeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (Objects.equals(currentUserId, followeeId)) {
            throw new BusinessException("Users cannot follow themselves");
        }
        User follower = getUser(currentUserId);
        User followee = getUser(followeeId);
        Workspace workspace = getWorkspaceForUser(follower);
        if (socialFollowRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new BusinessException("User already followed");
        }
        socialFollowRepository.save(new SocialFollow(workspace, follower, followee));
    }

    @Transactional
    public void unfollowUser(UUID followeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User follower = getUser(currentUserId);
        User followee = getUser(followeeId);
        socialFollowRepository.findByFollowerAndFollowee(follower, followee).ifPresent(socialFollowRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followeeId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (Objects.equals(currentUserId, followeeId)) {
            return false;
        }
        User follower = getUser(currentUserId);
        User followee = getUser(followeeId);
        return socialFollowRepository.existsByFollowerAndFollowee(follower, followee);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSocialSummary(UUID postId) {
        Workspace workspace = getWorkspaceForPost(postId);
        SocialPost post = getPost(postId, workspace);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("postId", post.getId());
        summary.put("likes", socialLikeRepository.findByPost(post).size());
        summary.put("favorites", socialFavoriteRepository.findByPost(post).size());
        summary.put("comments", socialCommentRepository.findByPostAndIsDeletedFalse(post, null).getSize());
        summary.put("tags", socialTagRepository.findByPost(post).stream().map(SocialTag::getTag).toList());
        return summary;
    }

    @Transactional(readOnly = true)
    public long getLikeCount(UUID postId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), SecurityUtils.getCurrentUserPrincipal().map(com.knowledgenetwork.security.UserPrincipal::getId).orElse(null));
        return socialLikeRepository.findByPost(post).size();
    }

    @Transactional(readOnly = true)
    public long getFavoriteCount(UUID postId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), SecurityUtils.getCurrentUserPrincipal().map(com.knowledgenetwork.security.UserPrincipal::getId).orElse(null));
        return socialFavoriteRepository.findByPost(post).size();
    }

    @Transactional(readOnly = true)
    public long getCommentCount(UUID postId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), SecurityUtils.getCurrentUserPrincipal().map(com.knowledgenetwork.security.UserPrincipal::getId).orElse(null));
        return socialCommentRepository.findByPostAndIsDeletedFalse(post, null).getSize();
    }

    @Transactional(readOnly = true)
    public List<String> getTags(UUID postId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), SecurityUtils.getCurrentUserPrincipal().map(com.knowledgenetwork.security.UserPrincipal::getId).orElse(null));
        return socialTagRepository.findByPost(post).stream().map(SocialTag::getTag).toList();
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(UUID postId, UUID userId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), userId);
        User user = getUser(userId);
        return socialLikeRepository.existsByPostAndUser(post, user);
    }

    @Transactional(readOnly = true)
    public boolean isFavoritedByUser(UUID postId, UUID userId) {
        SocialPost post = getPostById(postId);
        workspaceSecurityValidator.validateReadAccess(post.getWorkspace(), userId);
        User user = getUser(userId);
        return socialFavoriteRepository.existsByPostAndUser(post, user);
    }

    private Workspace getWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(UUID.fromString(workspaceId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
    }

    private Workspace getWorkspaceForPost(UUID postId) {
        SocialPost post = getPostById(postId);
        return post.getWorkspace();
    }

    private Workspace getWorkspaceForUser(User user) {
        return workspaceRepository.findByOwner(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "owner", user.getId()));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(UUID.fromString(userId.toString()))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private SocialPost getPost(UUID postId, Workspace workspace) {
        return socialPostRepository.findByIdAndWorkspaceAndIsDeletedFalse(UUID.fromString(postId.toString()), workspace)
                .orElseGet(() -> getPostById(postId));
    }

    private SocialPost getPostById(UUID postId) {
        return socialPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseGet(() -> socialPostRepository.findById(UUID.fromString(postId.toString()))
                        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId)));
    }
}
