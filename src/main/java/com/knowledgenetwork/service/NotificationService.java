package com.knowledgenetwork.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knowledgenetwork.common.exception.ResourceNotFoundException;
import com.knowledgenetwork.common.util.SecurityUtils;
import com.knowledgenetwork.domain.model.Notification;
import com.knowledgenetwork.domain.model.User;
import com.knowledgenetwork.domain.model.Workspace;
import com.knowledgenetwork.repository.NotificationRepository;
import com.knowledgenetwork.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Pageable pageable) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User recipient = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User recipient = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        return notificationRepository.countByRecipientAndIsReadFalse(recipient);
    }

    @Transactional
    public Notification markAsRead(UUID notificationId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User recipient = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        notificationRepository.markAllAsRead(recipient);
    }

    @Transactional
    public void createNotification(Workspace workspace, User recipient, String type, String message) {
        Notification notification = new Notification(workspace, recipient, type, message);
        notificationRepository.save(notification);
    }
}