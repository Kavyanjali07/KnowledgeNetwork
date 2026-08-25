package com.knowledgenetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.payload.request.WorkspaceUpdateRequest;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkspaceService workspaceService;

    private UserPrincipal userAPrincipal;
    private UUID userBWorkspaceId;

    @BeforeEach
    void setUp() {
        User userA = new User();
        userA.setId(UUID.randomUUID());
        userA.setEmail("usera@example.com");
        userA.setPasswordHash("hash");
        userA.setFirstName("User");
        userA.setLastName("A");

        userAPrincipal = UserPrincipal.fromUser(userA);
        userBWorkspaceId = UUID.randomUUID();
    }

    @Test
    void unauthenticatedUserAccessingProtectedEndpointShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserDeletingWorkspaceShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/workspaces/" + userBWorkspaceId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userATryingToDeleteUserBWorkspaceShouldReturn403Forbidden() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userAPrincipal, null, userAPrincipal.getAuthorities());

        doThrow(new AccessDeniedException("User must be an owner of this workspace to perform this action"))
                .when(workspaceService).deleteWorkspace(userBWorkspaceId);

        mockMvc.perform(delete("/api/v1/workspaces/" + userBWorkspaceId)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userATryingToModifyUserBWorkspaceShouldReturn403Forbidden() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userAPrincipal, null, userAPrincipal.getAuthorities());

        WorkspaceUpdateRequest updateRequest = new WorkspaceUpdateRequest();
        updateRequest.setName("Hacked Workspace Name");
        updateRequest.setVersion(1L);

        when(workspaceService.updateWorkspace(eq(userBWorkspaceId), any(WorkspaceUpdateRequest.class)))
                .thenThrow(new AccessDeniedException("User does not have permission to modify this workspace"));

        mockMvc.perform(put("/api/v1/workspaces/" + userBWorkspaceId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userATryingToReadUserBPrivateWorkspaceShouldReturn403Forbidden() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userAPrincipal, null, userAPrincipal.getAuthorities());

        when(workspaceService.getWorkspaceById(userBWorkspaceId))
                .thenThrow(new AccessDeniedException("User does not have access to this workspace"));

        mockMvc.perform(get("/api/v1/workspaces/" + userBWorkspaceId)
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }
}
