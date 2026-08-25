package com.knowledgenetwork.service;

import com.knowledgenetwork.domain.model.Node;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphSearchServiceTest {

    @Mock
    private NodeRepository nodeRepository;

    @InjectMocks
    private GraphSearchService graphSearchService;

    @Test
    void searchNodesShouldDelegateToRepositoryWithSpecificationsAndPagination() {
        Workspace workspace = new Workspace();
        workspace.setId(UUID.randomUUID());

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("label").ascending());
        when(nodeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        Page<Node> result = graphSearchService.searchNodes(workspace, "alpha", List.of("ai"), Visibility.PUBLIC, pageable);

        assertNotNull(result);
        verify(nodeRepository).findAll(any(Specification.class), eq(pageable));
    }
}
