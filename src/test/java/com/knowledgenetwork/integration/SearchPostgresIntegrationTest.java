package com.knowledgenetwork.integration;

import com.knowledgenetwork.config.AbstractPostgresIntegrationTest;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NodeRepository;
import com.knowledgenetwork.repository.NodeTypeRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.repository.WorkspaceRepository;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private NodeTypeRepository nodeTypeRepository;

    @Autowired
    private NodeRepository nodeRepository;

    private User userA;
    private User userB;
    private Workspace workspaceA;
    private Workspace workspaceB;

    @BeforeEach
    void setUp() {
        userA = userRepository.saveAndFlush(TestFixtures.createUser("search_a_" + UUID.randomUUID() + "@example.com", "User", "A"));
        userB = userRepository.saveAndFlush(TestFixtures.createUser("search_b_" + UUID.randomUUID() + "@example.com", "User", "B"));

        workspaceA = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("User A Workspace", userA));
        workspaceB = workspaceRepository.saveAndFlush(TestFixtures.createWorkspace("User B Workspace", userB));

        NodeType typeA = nodeTypeRepository.saveAndFlush(TestFixtures.createNodeType(workspaceA, "ConceptA"));
        NodeType typeB = nodeTypeRepository.saveAndFlush(TestFixtures.createNodeType(workspaceB, "ConceptB"));

        Node n1 = TestFixtures.createNode(workspaceA, typeA, "Quantum Computing Algorithm", 1.0, 1.0);
        Node n2 = TestFixtures.createNode(workspaceA, typeA, "Neural Network Optimization", 2.0, 2.0);
        Node n3 = TestFixtures.createNode(workspaceB, typeB, "Quantum Machine Learning Secret", 3.0, 3.0);

        nodeRepository.saveAndFlush(n1);
        nodeRepository.saveAndFlush(n2);
        nodeRepository.saveAndFlush(n3);
    }

    @Test
    void searchNodesInAccessibleWorkspaces_ShouldFilterBySearchQueryAndRespectTenantAccess() {
        Page<Node> resultsForUserA = nodeRepository.searchNodesInAccessibleWorkspaces(
                userA, "Quantum", PageRequest.of(0, 10)
        );

        assertNotNull(resultsForUserA);
        assertEquals(1, resultsForUserA.getTotalElements());
        assertEquals("Quantum Computing Algorithm", resultsForUserA.getContent().get(0).getLabel());

        Page<Node> resultsForUserB = nodeRepository.searchNodesInAccessibleWorkspaces(
                userB, "Quantum", PageRequest.of(0, 10)
        );

        assertNotNull(resultsForUserB);
        assertEquals(1, resultsForUserB.getTotalElements());
        assertEquals("Quantum Machine Learning Secret", resultsForUserB.getContent().get(0).getLabel());
    }
}
