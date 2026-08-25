package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.payload.request.GraphCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphUpdateRequest;
import com.knowledgenetwork.domain.payload.response.GraphResponse;
import com.knowledgenetwork.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GraphService graphService;

    @Test
    void createGraphShouldReturn201() throws Exception {
        GraphCreateRequest request = new GraphCreateRequest("Neural Networks Overview", "A graph of AI concepts", Visibility.PUBLIC);

        GraphResponse response = new GraphResponse();
        response.setId(UUID.randomUUID());
        response.setTitle("Neural Networks Overview");
        response.setName("Neural Networks Overview");
        response.setDescription("A graph of AI concepts");
        response.setVisibility(Visibility.PUBLIC);
        response.setVersion(1L);
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(graphService.createGraph(any(GraphCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Neural Networks Overview"))
                .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));
    }

    @Test
    void createGraphValidationFailureShouldReturn422() throws Exception {
        GraphCreateRequest request = new GraphCreateRequest("", "Invalid title graph", Visibility.PRIVATE);

        mockMvc.perform(post("/api/v1/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void getGraphsShouldReturn200() throws Exception {
        GraphResponse response = new GraphResponse();
        response.setId(UUID.randomUUID());
        response.setTitle("Knowledge Graph 1");
        response.setVisibility(Visibility.PRIVATE);

        PageImpl<GraphResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(graphService.getGraphs(anyInt(), anyInt(), anyString(), anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/graphs?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Knowledge Graph 1"));
    }

    @Test
    void getGraphByIdShouldReturn200() throws Exception {
        UUID graphId = UUID.randomUUID();
        GraphResponse response = new GraphResponse();
        response.setId(graphId);
        response.setTitle("Detailed Graph");
        response.setVisibility(Visibility.PUBLIC);

        when(graphService.getGraphById(eq(graphId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Detailed Graph"));
    }

    @Test
    void updateGraphShouldReturn200() throws Exception {
        UUID graphId = UUID.randomUUID();
        GraphUpdateRequest request = new GraphUpdateRequest("Updated Title", "Updated Desc", Visibility.PUBLIC, 1L);

        GraphResponse response = new GraphResponse();
        response.setId(graphId);
        response.setTitle("Updated Title");
        response.setDescription("Updated Desc");
        response.setVisibility(Visibility.PUBLIC);
        response.setVersion(2L);

        when(graphService.updateGraph(eq(graphId), any(GraphUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/graphs/" + graphId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.version").value(2));
    }

    @Test
    void deleteGraphShouldReturn200() throws Exception {
        UUID graphId = UUID.randomUUID();
        doNothing().when(graphService).deleteGraph(eq(graphId));

        mockMvc.perform(delete("/api/v1/graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
