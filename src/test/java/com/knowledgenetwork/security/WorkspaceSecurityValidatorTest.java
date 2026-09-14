package com.knowledgenetwork.security;

import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceSecurityValidatorTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceSecurityValidator validator;

    private User owner;
    private User editorUser;
    private User viewerUser;
    private User nonMemberUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());

        editorUser = new User();
        editorUser.setId(UUID.randomUUID());

        viewerUser = new User();
        viewerUser.setId(UUID.randomUUID());

        nonMemberUser = new User();
        nonMemberUser.setId(UUID.randomUUID());

        workspace = new Workspace();
        workspace.setId(UUID.randomUUID());
        workspace.setOwner(owner);
        workspace.setVisibility(Visibility.PRIVATE);
        workspace.setDeleted(false);
    }

    @Test
    void validateReadAccess_ShouldAllowOwner() {
        assertDoesNotThrow(() -> validator.validateReadAccess(workspace, owner.getId()));
    }

    @Test
    void validateReadAccess_ShouldAllowPublicWorkspaceWithoutUser() {
        workspace.setVisibility(Visibility.PUBLIC);
        assertDoesNotThrow(() -> validator.validateReadAccess(workspace, null));
    }

    @Test
    void validateReadAccess_ShouldRejectPrivateWorkspaceWithoutUser() {
        assertThrows(AccessDeniedException.class, () -> validator.validateReadAccess(workspace, null));
    }

    @Test
    void validateReadAccess_ShouldAllowMember() {
        when(userRepository.findById(editorUser.getId())).thenReturn(Optional.of(editorUser));
        when(workspaceMemberRepository.existsByWorkspaceAndUser(workspace, editorUser)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateReadAccess(workspace, editorUser.getId()));
    }

    @Test
    void validateReadAccess_ShouldRejectNonMember() {
        when(userRepository.findById(nonMemberUser.getId())).thenReturn(Optional.of(nonMemberUser));
        when(workspaceMemberRepository.existsByWorkspaceAndUser(workspace, nonMemberUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> validator.validateReadAccess(workspace, nonMemberUser.getId()));
    }

    @Test
    void validateWriteAccess_ShouldAllowOwner() {
        assertDoesNotThrow(() -> validator.validateWriteAccess(workspace, owner.getId()));
    }

    @Test
    void validateWriteAccess_ShouldAllowEditor() {
        WorkspaceMember member = new WorkspaceMember(workspace, editorUser, WorkspaceRole.EDITOR);
        when(userRepository.findById(editorUser.getId())).thenReturn(Optional.of(editorUser));
        when(workspaceMemberRepository.findByWorkspaceAndUser(workspace, editorUser)).thenReturn(Optional.of(member));

        assertDoesNotThrow(() -> validator.validateWriteAccess(workspace, editorUser.getId()));
    }

    @Test
    void validateWriteAccess_ShouldRejectViewer() {
        WorkspaceMember member = new WorkspaceMember(workspace, viewerUser, WorkspaceRole.VIEWER);
        when(userRepository.findById(viewerUser.getId())).thenReturn(Optional.of(viewerUser));
        when(workspaceMemberRepository.findByWorkspaceAndUser(workspace, viewerUser)).thenReturn(Optional.of(member));

        assertThrows(AccessDeniedException.class, () -> validator.validateWriteAccess(workspace, viewerUser.getId()));
    }

    @Test
    void validateWriteAccess_ShouldRejectDeletedWorkspace() {
        workspace.setDeleted(true);
        assertThrows(AccessDeniedException.class, () -> validator.validateWriteAccess(workspace, owner.getId()));
    }
}
