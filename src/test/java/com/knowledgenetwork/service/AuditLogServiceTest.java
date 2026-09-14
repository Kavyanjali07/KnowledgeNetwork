package com.knowledgenetwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.model.AuditLog;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.domain.payload.response.AuditLogResponse;
import com.knowledgenetwork.repository.AuditLogRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditLogService auditLogService;

    private UUID currentUserId;
    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setEmail("admin@knowledgenetwork.local");
        currentUser.setFirstName("Admin");
        currentUser.setLastName("User");

        UserPrincipal principal = UserPrincipal.fromUser(currentUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void recordEvent_ShouldSanitizeSecretsAndPersistAuditLog() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);

        UUID entityId = UUID.randomUUID();
        Map<String, Object> rawMetadata = Map.of(
                "password", "Secret123!",
                "token", "eyJhbGciOiJIUzI1NiJ9...",
                "workspaceName", "Core Graph"
        );

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditLog result = auditLogService.recordEvent(
                AuditAction.WORKSPACE_UPDATED,
                AuditEntityType.WORKSPACE,
                entityId,
                workspaceId,
                rawMetadata
        );

        assertNotNull(result);
        assertEquals(AuditAction.WORKSPACE_UPDATED, result.getAction());
        assertEquals(AuditEntityType.WORKSPACE, result.getEntityType());
        assertEquals(workspace, result.getWorkspace());
        assertEquals(entityId, result.getEntityId());
        assertEquals(currentUser, result.getUser());

        // Verify sensitive keys were omitted and non-sensitive preserved
        assertFalse(result.getSnapshotDelta().contains("password"));
        assertFalse(result.getSnapshotDelta().contains("token"));
        assertTrue(result.getSnapshotDelta().contains("Core Graph"));

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogs_WithWorkspaceId_ShouldValidateSecurityAccessAndReturnMappedResponses() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);

        Pageable pageable = PageRequest.of(0, 10);

        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID());
        log.setWorkspace(workspace);
        log.setUser(currentUser);
        log.setAction(AuditAction.GRAPH_CREATED);
        log.setEntityType(AuditEntityType.GRAPH);
        log.setSnapshotDelta("{\"title\":\"My Graph\"}");

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AuditLogResponse> result = auditLogService.getAuditLogs(
                workspaceId, null, AuditAction.GRAPH_CREATED, AuditEntityType.GRAPH, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AuditLogResponse response = result.getContent().get(0);
        assertEquals(AuditAction.GRAPH_CREATED, response.getAction());
        assertEquals("admin@knowledgenetwork.local", response.getActorEmail());
        verify(workspaceSecurityValidator).validateReadAccess(workspace, currentUserId);
    }
}
