package com.knowledgenetwork.service;

import com.knowledgenetwork.domain.model.SocialPost;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.SocialPostRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAuthorizationTest {

    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private SocialPostRepository socialPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SocialService socialService;

    private User userA;
    private User userB;
    private Workspace privateWorkspaceA;
    private SocialPost postA;

    @BeforeEach
    void setUp() {
        userA = TestFixtures.createUser("usera@example.com", "User", "A");
        userA.setId(UUID.randomUUID());

        userB = TestFixtures.createUser("userb@example.com", "User", "B");
        userB.setId(UUID.randomUUID());

        privateWorkspaceA = TestFixtures.createWorkspace("User A Workspace", userA);
        privateWorkspaceA.setId(UUID.randomUUID());

        postA = TestFixtures.createSocialPost(privateWorkspaceA, userA, "User A's private post");
        postA.setId(UUID.randomUUID());

        // Authenticate as User B
        UserPrincipal principalB = UserPrincipal.fromUser(userB);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principalB, null, principalB.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void userB_CannotCreatePostInUserAPrivateWorkspace() {
        when(workspaceRepository.findById(privateWorkspaceA.getId())).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateWriteAccess(privateWorkspaceA, userB.getId());

        assertThrows(AccessDeniedException.class, () ->
                socialService.createPost(privateWorkspaceA.getId(), "Unauthorized post", "GRAPH", privateWorkspaceA.getId()));
    }

    @Test
    void userB_CannotGetPostsInUserAPrivateWorkspace() {
        when(workspaceRepository.findById(privateWorkspaceA.getId())).thenReturn(Optional.of(privateWorkspaceA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateReadAccess(privateWorkspaceA, userB.getId());

        assertThrows(AccessDeniedException.class, () ->
                socialService.getPosts(privateWorkspaceA.getId(), null));
    }

    @Test
    void userB_CannotLikePostInUserAPrivateWorkspace() {
        when(socialPostRepository.findById(postA.getId())).thenReturn(Optional.of(postA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateReadAccess(privateWorkspaceA, userB.getId());

        assertThrows(AccessDeniedException.class, () ->
                socialService.likePost(postA.getId()));
    }

    @Test
    void userB_CannotCommentOnPostInUserAPrivateWorkspace() {
        when(socialPostRepository.findById(postA.getId())).thenReturn(Optional.of(postA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateWriteAccess(privateWorkspaceA, userB.getId());

        assertThrows(AccessDeniedException.class, () ->
                socialService.addComment(postA.getId(), "Unauthorized comment", null));
    }

    @Test
    void userB_CannotAddTagToPostInUserAPrivateWorkspace() {
        when(socialPostRepository.findById(postA.getId())).thenReturn(Optional.of(postA));
        doThrow(new AccessDeniedException("User does not have access to this workspace"))
                .when(workspaceSecurityValidator).validateWriteAccess(privateWorkspaceA, userB.getId());

        assertThrows(AccessDeniedException.class, () ->
                socialService.addTag(postA.getId(), "tag"));
    }
}
