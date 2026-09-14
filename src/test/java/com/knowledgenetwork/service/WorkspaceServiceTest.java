package com.knowledgenetwork.service;

import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.model.WorkspaceMember;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceMemberAddRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceUpdateRequest;
import com.knowledgenetwork.domain.payload.response.WorkspaceMemberResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.repository.EdgeTypeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceMemberRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NodeTypeRepository nodeTypeRepository;

    @Mock
    private EdgeTypeRepository edgeTypeRepository;

    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private WorkspaceService workspaceService;

    private UUID currentUserId;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setEmail("owner@example.com");
        currentUser.setFirstName("Owner");
        currentUser.setLastName("User");

        UserPrincipal principal = UserPrincipal.fromUser(currentUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createWorkspace_ShouldPersistWorkspaceAndLogAuditEvent() {
        WorkspaceCreateRequest request = new WorkspaceCreateRequest("My Knowledge Graph", "Description", Visibility.PRIVATE);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> {
            Workspace w = inv.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        WorkspaceResponse response = workspaceService.createWorkspace(request);

        assertNotNull(response);
        assertEquals("My Knowledge Graph", response.getName());
        verify(workspaceRepository).save(any(Workspace.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(auditLogService).recordEvent(
                eq(AuditAction.WORKSPACE_CREATED),
                eq(AuditEntityType.WORKSPACE),
                any(UUID.class),
                any(UUID.class),
                any()
        );
    }

    @Test
    void getWorkspace_ShouldValidateSecurityAccessAndReturnWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace("Graph Workspace", "Desc", currentUser, Visibility.PRIVATE);
        workspace.setId(workspaceId);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        WorkspaceResponse response = workspaceService.getWorkspaceById(workspaceId);

        assertNotNull(response);
        assertEquals("Graph Workspace", response.getName());
        verify(workspaceSecurityValidator).validateReadAccess(workspace, currentUserId);
    }

    @Test
    void updateWorkspace_ShouldUpdateDetailsAndLogAuditEvent() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace("Old Name", "Old Desc", currentUser, Visibility.PRIVATE);
        workspace.setId(workspaceId);
        workspace.setVersion(0L);

        WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("New Name", "New Desc", Visibility.PUBLIC, 0L);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, request);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        verify(workspaceSecurityValidator).validateWriteAccess(workspace, currentUserId);
        verify(auditLogService).recordEvent(
                eq(AuditAction.WORKSPACE_UPDATED),
                eq(AuditEntityType.WORKSPACE),
                eq(workspaceId),
                eq(workspaceId),
                any()
        );
    }

    @Test
    void addMember_ShouldAddMemberAndLogAuditEvent() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace("Workspace", "Desc", currentUser, Visibility.PRIVATE);
        workspace.setId(workspaceId);

        User memberUser = new User();
        memberUser.setId(UUID.randomUUID());
        memberUser.setEmail("member@example.com");
        memberUser.setFirstName("Member");

        WorkspaceMemberAddRequest request = new WorkspaceMemberAddRequest(memberUser.getId(), WorkspaceRole.EDITOR);

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(memberUser.getId())).thenReturn(Optional.of(memberUser));
        when(workspaceMemberRepository.existsByWorkspaceAndUser(workspace, memberUser)).thenReturn(false);
        when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenAnswer(inv -> {
            WorkspaceMember m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        WorkspaceMemberResponse response = workspaceService.addMember(workspaceId, request);

        assertNotNull(response);
        assertEquals("member@example.com", response.getUserEmail());
        assertEquals(WorkspaceRole.EDITOR, response.getRole());
        verify(workspaceSecurityValidator).validateOwnerAccess(workspace, currentUserId);
        verify(auditLogService).recordEvent(
                eq(AuditAction.MEMBER_ADDED),
                eq(AuditEntityType.WORKSPACE_MEMBER),
                any(UUID.class),
                eq(workspaceId),
                any()
        );
    }
}
