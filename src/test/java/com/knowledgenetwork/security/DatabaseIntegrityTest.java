package com.knowledgenetwork.security;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseIntegrityTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    private User owner;
    private User memberUser;
    private User strangerUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@example.com");

        memberUser = new User();
        memberUser.setId(UUID.randomUUID());
        memberUser.setEmail("editor@example.com");

        strangerUser = new User();
        strangerUser.setId(UUID.randomUUID());
        strangerUser.setEmail("stranger@example.com");

        workspace = new Workspace();
        workspace.setId(UUID.randomUUID());
        workspace.setName("Private Workspace");
        workspace.setOwner(owner);
    }

    @Test
    void ownerShouldHaveFullReadWriteOwnerAccess() {
        assertDoesNotThrow(() -> workspaceSecurityValidator.validateReadAccess(workspace, owner.getId()));
        assertDoesNotThrow(() -> workspaceSecurityValidator.validateWriteAccess(workspace, owner.getId()));
        assertDoesNotThrow(() -> workspaceSecurityValidator.validateOwnerAccess(workspace, owner.getId()));
    }

    @Test
    void strangerShouldBeDeniedReadWriteOwnerAccessToPrivateWorkspace() {
        when(userRepository.findById(strangerUser.getId())).thenReturn(Optional.of(strangerUser));
        when(workspaceMemberRepository.existsByWorkspaceAndUser(any(), any())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateReadAccess(workspace, strangerUser.getId()));

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateWriteAccess(workspace, strangerUser.getId()));

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateOwnerAccess(workspace, strangerUser.getId()));
    }

    @Test
    void viewerMemberShouldHaveReadAccessButDeniedWriteAccess() {
        when(userRepository.findById(memberUser.getId())).thenReturn(Optional.of(memberUser));
        when(workspaceMemberRepository.existsByWorkspaceAndUser(any(), any())).thenReturn(true);
        when(workspaceMemberRepository.findByWorkspaceAndUser(any(), any()))
                .thenReturn(Optional.of(new WorkspaceMember(workspace, memberUser, WorkspaceRole.VIEWER)));

        assertDoesNotThrow(() -> workspaceSecurityValidator.validateReadAccess(workspace, memberUser.getId()));

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateWriteAccess(workspace, memberUser.getId()));

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateOwnerAccess(workspace, memberUser.getId()));
    }
}

