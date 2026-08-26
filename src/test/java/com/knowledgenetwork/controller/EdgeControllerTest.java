package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.common.exception.DuplicateEdgeException;
import com.knowledgenetwork.domain.payload.request.EdgeCreateRequest;
import com.knowledgenetwork.domain.payload.request.EdgeUpdateRequest;
import com.knowledgenetwork.domain.payload.response.EdgeResponse;
import com.knowledgenetwork.service.EdgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
class EdgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EdgeService edgeService;

    @Test
    void createEdgeShouldReturn201() throws Exception {
        UUID graphId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(sourceId);
        request.setTargetNodeId(targetId);
        request.setRelationshipType("RELATED_TO");
        request.setLabel("Connects to");

        EdgeResponse response = new EdgeResponse();
        response.setId(UUID.randomUUID());
        response.setGraphId(graphId);
        response.setSourceNodeId(sourceId);
        response.setTargetNodeId(targetId);
        response.setRelationshipType("RELATED_TO");
        response.setLabel("Connects to");
        response.setVersion(1L);

        when(edgeService.createEdge(eq(graphId), any(EdgeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/graphs/" + graphId + "/edges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.relationshipType").value("RELATED_TO"))
                .andExpect(jsonPath("$.data.label").value("Connects to"));
    }

    @Test
    void createEdgeDuplicateShouldReturn409() throws Exception {
        UUID graphId = UUID.randomUUID();
        EdgeCreateRequest request = new EdgeCreateRequest();
        request.setSourceNodeId(UUID.randomUUID());
        request.setTargetNodeId(UUID.randomUUID());

        when(edgeService.createEdge(eq(graphId), any(EdgeCreateRequest.class)))
                .thenThrow(new DuplicateEdgeException("These ideas already have this connection."));

        mockMvc.perform(post("/api/v1/graphs/" + graphId + "/edges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getGraphEdgesShouldReturn200() throws Exception {
        UUID graphId = UUID.randomUUID();
        EdgeResponse response = new EdgeResponse();
        response.setId(UUID.randomUUID());
        response.setRelationshipType("DEPENDS_ON");

        when(edgeService.getEdgesByGraph(eq(graphId))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/graphs/" + graphId + "/edges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].relationshipType").value("DEPENDS_ON"));
    }

    @Test
    void getEdgeByIdShouldReturn200() throws Exception {
        UUID edgeId = UUID.randomUUID();
        EdgeResponse response = new EdgeResponse();
        response.setId(edgeId);
        response.setRelationshipType("PART_OF");

        when(edgeService.getEdgeById(eq(edgeId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/edges/" + edgeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.relationshipType").value("PART_OF"));
    }

    @Test
    void updateEdgeShouldReturn200() throws Exception {
        UUID edgeId = UUID.randomUUID();
        EdgeUpdateRequest request = new EdgeUpdateRequest();
        request.setLabel("Updated Edge");
        request.setVersion(1L);

        EdgeResponse response = new EdgeResponse();
        response.setId(edgeId);
        response.setLabel("Updated Edge");
        response.setVersion(2L);

        when(edgeService.updateEdge(eq(edgeId), any(EdgeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/edges/" + edgeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("Updated Edge"));
    }

    @Test
    void deleteEdgeShouldReturn204() throws Exception {
        UUID edgeId = UUID.randomUUID();
        doNothing().when(edgeService).deleteEdge(eq(edgeId));

        mockMvc.perform(delete("/api/v1/edges/" + edgeId))
                .andExpect(status().isNoContent());
    }
}
