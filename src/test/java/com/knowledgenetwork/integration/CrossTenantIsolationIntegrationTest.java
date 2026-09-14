package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.security.WorkspaceSecurityValidator;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CrossTenantIsolationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private WorkspaceSecurityValidator workspaceSecurityValidator;

    private User userA;
    private User userB;
    private Workspace workspaceA;
    private Workspace workspaceB;

    @BeforeEach
    void setUp() {
        userA = userRepository.saveAndFlush(TestFixtures.createUser("usera_" + UUID.randomUUID() + "@example.com", "User", "A"));
        userB = userRepository.saveAndFlush(TestFixtures.createUser("userb_" + UUID.randomUUID() + "@example.com", "User", "B"));

        workspaceA = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("Workspace A", userA));
        workspaceB = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("Workspace B", userB));

        Node nodeA = TestFixtures.createNode(workspaceA, "Node A Confidential", 10.0, 20.0);
        Node nodeB = TestFixtures.createNode(workspaceB, "Node B Confidential", 30.0, 40.0);
        nodeRepository.saveAndFlush(nodeA);
        nodeRepository.saveAndFlush(nodeB);
    }

    @Test
    void userA_ShouldNotHaveReadOrWriteAccessToWorkspaceB() {
        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateReadAccess(workspaceB, userA.getId())
        );

        assertThrows(AccessDeniedException.class, () ->
                workspaceSecurityValidator.validateWriteAccess(workspaceB, userA.getId())
        );
    }

    @Test
    void nodeQueries_ShouldStrictlyIsolateNodesByWorkspace() {
        List<Node> nodesA = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspaceA, null).getContent();
        List<Node> nodesB = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspaceB, null).getContent();

        assertEquals(1, nodesA.size());
        assertEquals("Node A Confidential", nodesA.get(0).getLabel());

        assertEquals(1, nodesB.size());
        assertEquals("Node B Confidential", nodesB.get(0).getLabel());

        // Ensure nodesA contains no node from Workspace B
        assertTrue(nodesA.stream().noneMatch(n -> n.getWorkspace().getId().equals(workspaceB.getId())));
    }
}
