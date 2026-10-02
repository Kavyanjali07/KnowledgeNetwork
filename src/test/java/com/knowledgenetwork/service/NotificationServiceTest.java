package com.knowledgenetwork.service;

import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.domain.model.Notification;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NotificationRepository;
import com.knowledgenetwork.repository.UserRepository;
import com.knowledgenetwork.security.UserPrincipal;
import com.knowledgenetwork.util.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User currentUser;
    private User otherUser;
    private Workspace workspace;
    private Notification notification;

    @BeforeEach
    void setUp() {
        currentUser = TestFixtures.createUser("user@example.com", "First", "Last");
        currentUser.setId(UUID.randomUUID());

        otherUser = TestFixtures.createUser("other@example.com", "Other", "User");
        otherUser.setId(UUID.randomUUID());

        workspace = TestFixtures.createWorkspace("Test Workspace", currentUser);
        workspace.setId(UUID.randomUUID());

        notification = TestFixtures.createNotification(workspace, currentUser, "LIKE", "User liked your post");
        notification.setId(UUID.randomUUID());

        UserPrincipal principal = UserPrincipal.fromUser(currentUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser, pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getNotifications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("LIKE", result.getContent().get(0).getType());
    }

    @Test
    void getUnreadCount_Success() {
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.countByRecipientAndIsReadFalse(currentUser)).thenReturn(5L);

        long unread = notificationService.getUnreadCount();

        assertEquals(5L, unread);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification marked = notificationService.markAsRead(notification.getId());

        assertTrue(marked.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_OtherUserRecipient_ThrowsResourceNotFoundException() {
        notification.setRecipient(otherUser);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(notification.getId()));
    }

    @Test
    void markAsRead_NotFound_ThrowsResourceNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(notificationRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(randomId));
    }

    @Test
    void markAllAsRead_Success() {
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.markAllAsRead(currentUser)).thenReturn(3);

        notificationService.markAllAsRead();

        verify(notificationRepository).markAllAsRead(currentUser);
    }

    @Test
    void createNotification_Success() {
        notificationService.createNotification(workspace, otherUser, "COMMENT", "New comment on post");

        verify(notificationRepository).save(any(Notification.class));
    }
}
