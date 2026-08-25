package com.knowledgenetwork.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgenetwork.common.dto.ApiResponse;
import com.knowledgenetwork.common.dto.PageResponse;
import com.knowledgenetwork.domain.model.Notification;
import com.knowledgenetwork.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get current user's notifications")
    public ResponseEntity<ApiResponse<PageResponse<Notification>>> getNotifications(
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotifications(pageable);
        PageResponse<Notification> pageResponse = PageResponse.from(notifications);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", pageResponse));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(
            @PathVariable UUID id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all current user's notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal com.knowledgenetwork.security.UserPrincipal userPrincipal) {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }
}