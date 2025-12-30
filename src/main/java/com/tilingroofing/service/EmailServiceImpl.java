package com.tilingroofing.service;

import com.tilingroofing.domain.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of EmailService.
 * Handles sending email notifications using async processing.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final TemplateEngine templateEngine;
    private final String adminEmail;
    private final String fromEmail;

    public EmailServiceImpl(
            TemplateEngine templateEngine,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${spring.mail.username:noreply@tilingroofing.com.au}") String fromEmail
    ) {
        this.templateEngine = templateEngine;
        this.adminEmail = adminEmail;
        this.fromEmail = fromEmail;
    }

    @Override
    @Async
    public void sendCustomerConfirmation(Booking booking) {
        // Email functionality disabled
        log.debug("Email sending disabled - skipping customer confirmation for booking: {}", 
                booking.getBookingRef());
    }

    @Override
    @Async
    public void sendAdminNotification(Booking booking) {
        // Email functionality disabled
        log.debug("Email sending disabled - skipping admin notification for booking: {}", 
                booking.getBookingRef());
    }

    @Override
    @Async
    public void sendStatusUpdateEmail(Booking booking) {
        // Email functionality disabled
        log.debug("Email sending disabled - skipping status update for booking: {}", 
                booking.getBookingRef());
    }

    /**
     * Creates a Thymeleaf context with booking details.
     */
    private Context createBookingContext(Booking booking) {
        Context context = new Context();
        context.setVariable("booking", booking);
        context.setVariable("bookingRef", booking.getBookingRef());
        context.setVariable("customerName", booking.getUser().getDisplayName());
        context.setVariable("serviceId", booking.getServiceId());
        context.setVariable("jobSize", booking.getJobSize().getValue());
        context.setVariable("preferredDate", booking.getPreferredDate());
        context.setVariable("timeSlot", booking.getTimeSlot().getValue());
        context.setVariable("suburb", booking.getSuburb());
        context.setVariable("postcode", booking.getPostcode());
        context.setVariable("description", booking.getDescription());
        context.setVariable("status", booking.getStatus().getValue());
        context.setVariable("customerEmail", booking.getUser().getEmail());
        context.setVariable("customerPhone", booking.getCustomerPhone());
        return context;
    }
}

