package com.knowledgenetwork.journey;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.model.AuditLog;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.payload.request.GraphCreateRequest;
import com.knowledgenetwork.domain.payload.request.LoginRequest;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.RegisterRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.response.AuthResponse;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.repository.AuditLogRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.AuthService;
import com.knowledgenetwork.service.GraphService;
import com.knowledgenetwork.service.GraphVersioningService;
import com.knowledgenetwork.service.NodeService;
import com.knowledgenetwork.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FullApplicationLifecycleJourneyTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private GraphVersioningService graphVersioningService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void fullApplicationLifecycleJourney_ShouldExecuteAllPhasesSuccessfully() {
        String email = "lifecycle_" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";

        // 1. Register User
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail(email);
        registerReq.setPassword(password);
        registerReq.setFirstName("Lifecycle");
        registerReq.setLastName("User");
        authService.register(registerReq);

        // Auto-verify email for integration test flow
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setEmailVerified(true);
            userRepository.saveAndFlush(u);
        });

        // 2. Login User
        AuthResponse authResponse = authService.login(new LoginRequest(email, password));
        assertNotNull(authResponse);
        assertNotNull(authResponse.getAccessToken());

        // Set Security Context
        var user = userRepository.findByEmail(email).orElseThrow();
        UserPrincipal principal = UserPrincipal.fromUser(user);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 3. Create Workspace
        WorkspaceResponse workspace = workspaceService.createWorkspace(
                new WorkspaceCreateRequest("E2E Workspace", "Description", Visibility.PRIVATE)
        );
        assertNotNull(workspace);
        UUID workspaceId = workspace.getId();

        // 4. Create Graph
        GraphCreateRequest graphReq = new GraphCreateRequest();
        graphReq.setTitle("E2E Graph");
        graphReq.setDescription("Knowledge Graph");
        graphReq.setVisibility(Visibility.PRIVATE);
        GraphResponse graph = graphService.createGraph(graphReq);
        assertNotNull(graph);

        // 5. Create Node
        NodeCreateRequest nodeReq = new NodeCreateRequest();
        nodeReq.setLabel("Root Concept");
        nodeReq.setPositionX(100.0);
        nodeReq.setPositionY(200.0);
        nodeReq.setWorkspaceId(workspaceId);
        NodeResponse node = nodeService.createNode(workspaceId, nodeReq);
        assertNotNull(node);
        assertEquals("Root Concept", node.getLabel());

        // 6. Create Version Snapshot
        GraphVersion version = graphVersioningService.createSnapshot(workspaceId, "v1.0-e2e", "E2E Snapshot");
        assertNotNull(version);
        assertEquals("v1.0-e2e", version.getLabel());

        // 7. Verify Audit Log Trail in PostgreSQL
        List<AuditLog> logs = auditLogRepository.findByWorkspaceId(workspaceId);
        assertFalse(logs.isEmpty(), "Audit log records should be persisted for workspace operations");
        assertTrue(logs.stream().anyMatch(l -> AuditAction.WORKSPACE_CREATED.equals(l.getAction())));
        assertTrue(logs.stream().anyMatch(l -> AuditAction.GRAPH_VERSION_CREATED.equals(l.getAction())));

        // 8. Logout
        authService.logout(user.getId());
        SecurityContextHolder.clearContext();
    }
}
