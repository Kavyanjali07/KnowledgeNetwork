package com.knowledgenetwork.controller;

import com.knowledgenetwork.domain.enums.AuditAction;
import com.knowledgenetwork.domain.enums.AuditEntityType;
import com.knowledgenetwork.domain.payload.response.AuditLogResponse;
import com.knowledgenetwork.service.AuditLogService;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser
    void getAuditLogs_ShouldReturnAuditLogPage() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        AuditLogResponse logResponse = new AuditLogResponse();
        logResponse.setId(UUID.randomUUID());
        logResponse.setWorkspaceId(workspaceId);
        logResponse.setActorEmail("user@example.com");
        logResponse.setActorName("User Name");
        logResponse.setAction(AuditAction.LOGIN_SUCCESS);
        logResponse.setEntityType(AuditEntityType.AUTH);
        logResponse.setMetadata(Map.of("ip", "127.0.0.1"));
        logResponse.setTimestamp(Instant.now());

        when(auditLogService.getAuditLogs(
                eq(workspaceId), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(logResponse)));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("workspaceId", workspaceId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.content[0].actorEmail").value("user@example.com"))
                .andExpect(jsonPath("$.content[0].entityType").value("AUTH"));
    }
}
