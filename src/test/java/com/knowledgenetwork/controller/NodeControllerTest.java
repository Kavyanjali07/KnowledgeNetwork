package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.payload.request.NodeCreateRequest;
import com.knowledgenetwork.domain.payload.request.NodeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.NodeResponse;
import com.knowledgenetwork.service.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NodeService nodeService;

    @Test
    void createNodeShouldReturn201() throws Exception {
        UUID graphId = UUID.randomUUID();
        NodeCreateRequest request = new NodeCreateRequest(graphId, UUID.randomUUID(), "Graph Concept", Map.of("description", "Node details"));
        request.setPositionX(150.0);
        request.setPositionY(250.0);

        NodeResponse response = new NodeResponse();
        response.setId(UUID.randomUUID());
        response.setWorkspaceId(graphId);
        response.setLabel("Graph Concept");
        response.setPositionX(150.0);
        response.setPositionY(250.0);
        response.setVersion(1L);

        when(nodeService.createNode(eq(graphId), any(NodeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("Graph Concept"))
                .andExpect(jsonPath("$.data.positionX").value(150.0));
    }

    @Test
    void createNodeValidationFailureShouldReturn422() throws Exception {
        UUID graphId = UUID.randomUUID();
        NodeCreateRequest request = new NodeCreateRequest(graphId, null, "", null); // Blank label triggers validation error

        mockMvc.perform(post("/api/v1/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void getGraphNodesShouldReturn200() throws Exception {
        UUID graphId = UUID.randomUUID();
        NodeResponse response = new NodeResponse();
        response.setId(UUID.randomUUID());
        response.setLabel("Node in Graph");

        PageImpl<NodeResponse> page = new PageImpl<>(List.of(response));
        when(nodeService.getGraphNodes(eq(graphId), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/graphs/" + graphId + "/nodes?page=0&size=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].label").value("Node in Graph"));
    }

    @Test
    void getNodeByIdShouldReturn200() throws Exception {
        UUID nodeId = UUID.randomUUID();
        NodeResponse response = new NodeResponse();
        response.setId(nodeId);
        response.setLabel("Specific Node");

        when(nodeService.getNodeById(eq(nodeId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("Specific Node"));
    }

    @Test
    void updateNodeShouldReturn200() throws Exception {
        UUID nodeId = UUID.randomUUID();
        NodeUpdateRequest request = new NodeUpdateRequest(null, "Updated Title", Map.of("description", "Updated content"), 1L);

        NodeResponse response = new NodeResponse();
        response.setId(nodeId);
        response.setLabel("Updated Title");
        response.setVersion(2L);

        when(nodeService.updateNode(eq(nodeId), any(NodeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/nodes/" + nodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("Updated Title"))
                .andExpect(jsonPath("$.data.version").value(2));
    }

    @Test
    void deleteNodeShouldReturn200() throws Exception {
        UUID nodeId = UUID.randomUUID();
        doNothing().when(nodeService).deleteNode(eq(nodeId));

        mockMvc.perform(delete("/api/v1/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
