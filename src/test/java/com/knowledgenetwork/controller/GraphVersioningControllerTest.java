package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.model.GraphVersion;
import com.knowledgenetwork.domain.payload.request.GraphForkCreateRequest;
import com.knowledgenetwork.domain.payload.request.GraphSnapshotCreateRequest;
import com.knowledgenetwork.domain.payload.response.GraphForkResponse;
import com.knowledgenetwork.service.GraphVersioningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("local")
class GraphVersioningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GraphVersioningService graphVersioningService;

    private UUID workspaceId;
    private UUID versionId;
    private GraphVersion graphVersion;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        versionId = UUID.randomUUID();
        graphVersion = new GraphVersion();
        graphVersion.setId(versionId);
        graphVersion.setVersionNumber(1L);
        graphVersion.setLabel("v1.0.0");
        graphVersion.setDescription("Initial Release");
    }

    @Test
    @WithMockUser
    void createSnapshot_ShouldReturn201Created() throws Exception {
        GraphSnapshotCreateRequest request = new GraphSnapshotCreateRequest("v1.0.0", "Initial Release");

        when(graphVersioningService.createSnapshot(eq(workspaceId), eq("v1.0.0"), eq("Initial Release")))
                .thenReturn(graphVersion);

        mockMvc.perform(post("/api/v1/graphs/{workspaceId}/snapshots", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(versionId.toString()))
                .andExpect(jsonPath("$.data.label").value("v1.0.0"));
    }

    @Test
    @WithMockUser
    void listHistory_ShouldReturnPaginatedVersions() throws Exception {
        when(graphVersioningService.listHistory(eq(workspaceId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(graphVersion)));

        mockMvc.perform(get("/api/v1/graphs/{workspaceId}/versions", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(versionId.toString()));
    }

    @Test
    @WithMockUser
    void restoreVersion_ShouldReturnRestoredVersion() throws Exception {
        when(graphVersioningService.restoreVersion(workspaceId, versionId)).thenReturn(graphVersion);

        mockMvc.perform(post("/api/v1/graphs/{workspaceId}/versions/{versionId}/restore", workspaceId, versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(versionId.toString()));
    }

    @Test
    @WithMockUser
    void createFork_ShouldReturn201Created() throws Exception {
        GraphForkCreateRequest request = new GraphForkCreateRequest(versionId, "Forked Graph", "Description");
        GraphForkResponse forkResponse = new GraphForkResponse();
        forkResponse.setId(UUID.randomUUID());
        forkResponse.setName("Forked Graph");

        when(graphVersioningService.createFork(eq(workspaceId), eq(versionId), eq("Forked Graph"), eq("Description")))
                .thenReturn(forkResponse);

        mockMvc.perform(post("/api/v1/graphs/{workspaceId}/forks", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Forked Graph"));
    }
}
