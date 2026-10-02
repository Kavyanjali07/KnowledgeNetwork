package com.knowledgenetwork.controller;

import com.knowledgenetwork.domain.model.Notification;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.service.NotificationService;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private User recipient;
    private Workspace workspace;
    private Notification notification;

    @BeforeEach
    void setUp() {
        recipient = TestFixtures.createUser("recipient@example.com", "Recipient", "User");
        recipient.setId(UUID.randomUUID());

        workspace = TestFixtures.createWorkspace("Test Workspace", recipient);
        workspace.setId(UUID.randomUUID());

        notification = TestFixtures.createNotification(workspace, recipient, "LIKE", "User liked your post");
        notification.setId(UUID.randomUUID());
    }

    @Test
    void getNotificationsShouldReturn200() throws Exception {
        when(notificationService.getNotifications(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        mockMvc.perform(get("/api/v1/notifications?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].type").value("LIKE"))
                .andExpect(jsonPath("$.data.content[0].message").value("User liked your post"));
    }

    @Test
    void markAsReadShouldReturn200() throws Exception {
        notification.setRead(true);
        when(notificationService.markAsRead(eq(notification.getId()))).thenReturn(notification);

        mockMvc.perform(patch("/api/v1/notifications/" + notification.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    void markAllAsReadShouldReturn200() throws Exception {
        doNothing().when(notificationService).markAllAsRead();

        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUnreadCountShouldReturn200() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(7L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(7));
    }
}
