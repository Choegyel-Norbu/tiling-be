package com.tilingroofing.service;

import com.tilingroofing.api.dto.response.NotificationResponse;
import com.tilingroofing.api.dto.response.PagedResponse;

/**
 * Service interface for managing notifications.
 * Defines the contract for notification operations.
 */
public interface NotificationService {

    /**
     * Retrieves all notifications with pagination.
     * 
     * @param page Page number (0-based)
     * @param limit Page size
     * @return PagedResponse containing list of notifications
     */
    PagedResponse<NotificationResponse> getAllNotifications(int page, int limit);

    /**
     * Creates a notification for a booking.
     * 
     * @param bookingId The booking ID
     * @param message The notification message
     * @return Created NotificationResponse
     */
    NotificationResponse createNotification(Long bookingId, String message);

    /**
     * Marks a notification as read.
     * 
     * @param notificationId The notification ID
     * @return Updated NotificationResponse
     */
    NotificationResponse markAsRead(Long notificationId);
}

