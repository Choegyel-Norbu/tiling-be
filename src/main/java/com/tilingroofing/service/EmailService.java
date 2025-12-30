package com.tilingroofing.service;

import com.tilingroofing.domain.entity.Booking;

/**
 * Service interface for sending email notifications.
 * Defines the contract for email operations.
 */
public interface EmailService {

    /**
     * Sends a booking confirmation email to the customer.
     * 
     * @param booking The booking to send confirmation for
     */
    void sendCustomerConfirmation(Booking booking);

    /**
     * Sends a new booking notification to the admin.
     * 
     * @param booking The booking to notify about
     */
    void sendAdminNotification(Booking booking);

    /**
     * Sends a status update email to the customer.
     * 
     * @param booking The booking with updated status
     */
    void sendStatusUpdateEmail(Booking booking);
}
