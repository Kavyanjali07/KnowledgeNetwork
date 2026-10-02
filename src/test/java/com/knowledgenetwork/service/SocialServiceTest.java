package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.common.exception.ResourceNotFoundException;
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
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private SocialPostRepository socialPostRepository;
    @Mock
    private SocialLikeRepository socialLikeRepository;
    @Mock
    private SocialFavoriteRepository socialFavoriteRepository;
    @Mock
    private SocialCommentRepository socialCommentRepository;
    @Mock
    private SocialTagRepository socialTagRepository;
    @Mock
    private SocialFollowRepository socialFollowRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SocialService socialService;

    private User currentUser;
    private User otherUser;
    private Workspace workspace;
    private SocialPost post;

    @BeforeEach
    void setUp() {
        currentUser = TestFixtures.createUser("current@example.com", "Current", "User");
        currentUser.setId(UUID.randomUUID());

        otherUser = TestFixtures.createUser("other@example.com", "Other", "User");
        otherUser.setId(UUID.randomUUID());

        workspace = TestFixtures.createWorkspace("Test Workspace", currentUser);
        workspace.setId(UUID.randomUUID());

        post = TestFixtures.createSocialPost(workspace, currentUser, "Hello world!");
        post.setId(UUID.randomUUID());

        UserPrincipal principal = UserPrincipal.fromUser(currentUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createPost_Success() {
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialPostRepository.save(any(SocialPost.class))).thenAnswer(i -> i.getArgument(0));

        SocialPost created = socialService.createPost(workspace.getId(), "Test Post Content", "GRAPH", workspace.getId());

        assertNotNull(created);
        assertEquals("Test Post Content", created.getContent());
        verify(workspaceSecurityValidator).validateWriteAccess(workspace, currentUser.getId());
        verify(socialPostRepository).save(any(SocialPost.class));
    }

    @Test
    void getPosts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SocialPost> page = new PageImpl<>(List.of(post));

        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(socialPostRepository.findByWorkspaceAndIsDeletedFalse(workspace, pageable)).thenReturn(page);

        Page<SocialPost> result = socialService.getPosts(workspace.getId(), pageable);

        assertEquals(1, result.getTotalElements());
        verify(workspaceSecurityValidator).validateReadAccess(workspace, currentUser.getId());
    }

    @Test
    void likePost_Success_NotifiesOtherUserAuthor() {
        post.setAuthor(otherUser);
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialLikeRepository.existsByPostAndUser(post, currentUser)).thenReturn(false);

        socialService.likePost(post.getId());

        verify(socialLikeRepository).save(any(SocialLike.class));
        verify(notificationService).createNotification(eq(workspace), eq(otherUser), eq("LIKE"), anyString());
    }

    @Test
    void likePost_Success_SelfPost_NoNotification() {
        post.setAuthor(currentUser);
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialLikeRepository.existsByPostAndUser(post, currentUser)).thenReturn(false);

        socialService.likePost(post.getId());

        verify(socialLikeRepository).save(any(SocialLike.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    void likePost_AlreadyLiked_ThrowsException() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialLikeRepository.existsByPostAndUser(post, currentUser)).thenReturn(true);

        assertThrows(BusinessException.class, () -> socialService.likePost(post.getId()));
    }

    @Test
    void unlikePost_Success() {
        SocialLike like = new SocialLike(workspace, post, currentUser);
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialLikeRepository.findByPostAndUser(post, currentUser)).thenReturn(Optional.of(like));

        socialService.unlikePost(post.getId());

        verify(socialLikeRepository).delete(like);
    }

    @Test
    void favoritePost_Success() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialFavoriteRepository.existsByPostAndUser(post, currentUser)).thenReturn(false);

        socialService.favoritePost(post.getId());

        verify(socialFavoriteRepository).save(any(SocialFavorite.class));
    }

    @Test
    void favoritePost_AlreadyFavorited_ThrowsException() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialFavoriteRepository.existsByPostAndUser(post, currentUser)).thenReturn(true);

        assertThrows(BusinessException.class, () -> socialService.favoritePost(post.getId()));
    }

    @Test
    void unfavoritePost_Success() {
        SocialFavorite favorite = new SocialFavorite(workspace, post, currentUser);
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialFavoriteRepository.findByPostAndUser(post, currentUser)).thenReturn(Optional.of(favorite));

        socialService.unfavoritePost(post.getId());

        verify(socialFavoriteRepository).delete(favorite);
    }

    @Test
    void addComment_TopLevel_Success() {
        post.setAuthor(otherUser);
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialCommentRepository.save(any(SocialComment.class))).thenAnswer(i -> i.getArgument(0));

        SocialComment comment = socialService.addComment(post.getId(), "Great post!", null);

        assertNotNull(comment);
        assertEquals("Great post!", comment.getContent());
        verify(notificationService).createNotification(eq(workspace), eq(otherUser), eq("COMMENT"), anyString());
    }

    @Test
    void addComment_Threaded_Success() {
        UUID parentId = UUID.randomUUID();
        SocialComment parent = new SocialComment(workspace, post, otherUser, "Parent comment");
        parent.setId(parentId);

        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialCommentRepository.findByIdAndWorkspaceAndIsDeletedFalse(parentId, workspace)).thenReturn(Optional.of(parent));
        when(socialCommentRepository.save(any(SocialComment.class))).thenAnswer(i -> i.getArgument(0));

        SocialComment comment = socialService.addComment(post.getId(), "Reply comment", parentId);

        assertNotNull(comment);
        assertEquals(parent, comment.getParentComment());
    }

    @Test
    void addComment_ParentNotFound_ThrowsException() {
        UUID parentId = UUID.randomUUID();
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(socialCommentRepository.findByIdAndWorkspaceAndIsDeletedFalse(parentId, workspace)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> socialService.addComment(post.getId(), "Reply comment", parentId));
    }

    @Test
    void addTag_Success() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(socialTagRepository.existsByPostAndTag(post, "knowledge")).thenReturn(false);

        socialService.addTag(post.getId(), "Knowledge");

        verify(socialTagRepository).save(any(SocialTag.class));
    }

    @Test
    void addTag_Duplicate_ThrowsException() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(socialTagRepository.existsByPostAndTag(post, "knowledge")).thenReturn(true);

        assertThrows(BusinessException.class, () -> socialService.addTag(post.getId(), "Knowledge"));
    }

    @Test
    void followUser_Success() {
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(workspaceRepository.findByOwner(currentUser)).thenReturn(List.of(workspace));
        when(socialFollowRepository.existsByFollowerAndFollowee(currentUser, otherUser)).thenReturn(false);

        socialService.followUser(otherUser.getId());

        verify(socialFollowRepository).save(any(SocialFollow.class));
    }

    @Test
    void followUser_SelfFollow_ThrowsException() {
        assertThrows(BusinessException.class, () -> socialService.followUser(currentUser.getId()));
    }

    @Test
    void followUser_AlreadyFollowed_ThrowsException() {
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(workspaceRepository.findByOwner(currentUser)).thenReturn(List.of(workspace));
        when(socialFollowRepository.existsByFollowerAndFollowee(currentUser, otherUser)).thenReturn(true);

        assertThrows(BusinessException.class, () -> socialService.followUser(otherUser.getId()));
    }

    @Test
    void unfollowUser_Success() {
        SocialFollow follow = new SocialFollow(workspace, currentUser, otherUser);
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(socialFollowRepository.findByFollowerAndFollowee(currentUser, otherUser)).thenReturn(Optional.of(follow));

        socialService.unfollowUser(otherUser.getId());

        verify(socialFollowRepository).delete(follow);
    }

    @Test
    void isFollowing_SelfCheck_ReturnsFalse() {
        assertFalse(socialService.isFollowing(currentUser.getId()));
    }

    @Test
    void isFollowing_Success() {
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));
        when(socialFollowRepository.existsByFollowerAndFollowee(currentUser, otherUser)).thenReturn(true);

        assertTrue(socialService.isFollowing(otherUser.getId()));
    }

    @Test
    void getSocialSummary_Success() {
        when(socialPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));
        when(socialLikeRepository.findByPost(post)).thenReturn(List.of(new SocialLike(workspace, post, currentUser)));
        when(socialFavoriteRepository.findByPost(post)).thenReturn(List.of());
        when(socialCommentRepository.findByPostAndIsDeletedFalse(post, null)).thenReturn(new PageImpl<>(List.of()));
        when(socialTagRepository.findByPost(post)).thenReturn(List.of(new SocialTag(workspace, post, "graph")));

        Map<String, Object> summary = socialService.getSocialSummary(post.getId());

        assertNotNull(summary);
        assertEquals(post.getId(), summary.get("postId"));
        assertEquals(1, summary.get("likes"));
        assertEquals(0, summary.get("favorites"));
        assertEquals(0, summary.get("comments"));
        assertEquals(List.of("graph"), summary.get("tags"));
    }
}
