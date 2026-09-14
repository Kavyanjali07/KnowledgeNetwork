package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.model.Visibility;
import com.knowledgenetwork.domain.enums.WorkspaceRole;
import com.knowledgenetwork.domain.payload.request.WorkspaceCreateRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceMemberAddRequest;
import com.knowledgenetwork.domain.payload.request.WorkspaceUpdateRequest;
import com.knowledgenetwork.domain.payload.response.WorkspaceMemberResponse;
import com.knowledgenetwork.domain.payload.response.WorkspaceResponse;
import com.knowledgenetwork.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("local")
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkspaceService workspaceService;

    private UUID workspaceId;
    private WorkspaceResponse workspaceResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        workspaceResponse = new WorkspaceResponse();
        workspaceResponse.setId(workspaceId);
        workspaceResponse.setName("Test Workspace");
        workspaceResponse.setDescription("Test Description");
        workspaceResponse.setVisibility(Visibility.PRIVATE);
    }

    @Test
    @WithMockUser
    void getWorkspaces_ShouldReturnWorkspaceList() throws Exception {
        when(workspaceService.getWorkspacesForCurrentUser()).thenReturn(List.of(workspaceResponse));

        mockMvc.perform(get("/api/v1/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Test Workspace"));
    }

    @Test
    @WithMockUser
    void createWorkspace_ShouldReturn201Created() throws Exception {
        WorkspaceCreateRequest request = new WorkspaceCreateRequest("New Workspace", "Description", Visibility.PRIVATE);

        when(workspaceService.createWorkspace(any(WorkspaceCreateRequest.class))).thenReturn(workspaceResponse);

        mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(workspaceId.toString()));
    }

    @Test
    @WithMockUser
    void getWorkspace_ShouldReturnWorkspaceById() throws Exception {
        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(workspaceResponse);

        mockMvc.perform(get("/api/v1/workspaces/{id}", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(workspaceId.toString()));
    }

    @Test
    @WithMockUser
    void updateWorkspace_ShouldReturnUpdatedWorkspace() throws Exception {
        WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("Updated Name", "Updated Desc", Visibility.PUBLIC, null);

        when(workspaceService.updateWorkspace(eq(workspaceId), any(WorkspaceUpdateRequest.class))).thenReturn(workspaceResponse);

        mockMvc.perform(put("/api/v1/workspaces/{id}", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void deleteWorkspace_ShouldReturnSuccess() throws Exception {
        doNothing().when(workspaceService).deleteWorkspace(workspaceId);

        mockMvc.perform(delete("/api/v1/workspaces/{id}", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void addMember_ShouldReturn201Created() throws Exception {
        UUID memberId = UUID.randomUUID();
        WorkspaceMemberAddRequest request = new WorkspaceMemberAddRequest(memberId, WorkspaceRole.EDITOR);

        WorkspaceMemberResponse memberResponse = new WorkspaceMemberResponse();
        memberResponse.setId(UUID.randomUUID());
        memberResponse.setWorkspaceId(workspaceId);
        memberResponse.setUserId(memberId);
        memberResponse.setRole(WorkspaceRole.EDITOR);

        when(workspaceService.addMember(eq(workspaceId), any(WorkspaceMemberAddRequest.class))).thenReturn(memberResponse);

        mockMvc.perform(post("/api/v1/workspaces/{id}/members", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("EDITOR"));
    }
}
