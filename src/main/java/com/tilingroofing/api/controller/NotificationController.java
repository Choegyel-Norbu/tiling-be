package com.tilingroofing.api.controller;

import com.tilingroofing.api.dto.response.ApiResponse;
import com.tilingroofing.api.dto.response.NotificationResponse;
import com.tilingroofing.api.dto.response.PagedResponse;
import com.tilingroofing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations.
 * Provides endpoints for retrieving notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications with pagination.
     */
    @GetMapping
    @Operation(
            summary = "Get all notifications",
            description = "Retrieves all notifications ordered by creation date (newest first) with pagination. Includes essential booking and user information."
    )
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getAllNotifications(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int limit
    ) {
        // Enforce maximum page size
        limit = Math.min(limit, 100);
        
        PagedResponse<NotificationResponse> notifications = notificationService.getAllNotifications(page, limit);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Marks a notification as read.
     */
    @PatchMapping("/{id}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification as read by its ID"
    )
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "Notification ID")
            @PathVariable Long id
    ) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
    }
}

