package com.knowledgenetwork.repository;

import com.knowledgenetwork.config.AuditConfig;
import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.NodeType;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AuditConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NodeRepositoryTest {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByWorkspaceAndIsDeletedFalseShouldReturnActiveNodes() {
        User owner = new User();
        owner.setEmail("repo-owner@example.com");
        owner.setPasswordHash("hash");
        owner.setFirstName("Repo");
        owner.setLastName("Owner");
        owner = entityManager.persistAndFlush(owner);

        Workspace workspace = new Workspace();
        workspace.setName("Test workspace");
        workspace.setDescription("desc");
        workspace.setOwner(owner);
        workspace = workspaceRepository.save(workspace);

        NodeType nodeType = new NodeType();
        nodeType.setWorkspace(workspace);
        nodeType.setName("Concept");
        entityManager.persistAndFlush(nodeType);

        Node node = new Node();
        node.setWorkspace(workspace);
        node.setNodeType(nodeType);
        node.setLabel("Alpha");
        node.setAttributes(java.util.Map.of());
        node.setVisibility(Visibility.PUBLIC);
        nodeRepository.save(node);

        Page<Node> nodePage = nodeRepository.findByWorkspaceAndIsDeletedFalse(workspace, Pageable.unpaged());
        List<Node> nodes = nodePage.getContent();

        assertFalse(nodes.isEmpty());
        assertEquals("Alpha", nodes.get(0).getLabel());
    }
}
