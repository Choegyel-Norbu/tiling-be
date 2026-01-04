package com.tilingroofing.service;

import com.tilingroofing.domain.entity.Booking;
import com.tilingroofing.domain.repository.BookingRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    private final JavaMailSender mailSender;
    private final BookingRepository bookingRepository;
    private final String adminEmail;
    private final String fromEmail;

    public EmailServiceImpl(
            TemplateEngine templateEngine,
            JavaMailSender mailSender,
            BookingRepository bookingRepository,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${spring.mail.username:noreply@tilingroofing.com.au}") String fromEmail
    ) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
        this.bookingRepository = bookingRepository;
        this.adminEmail = adminEmail;
        this.fromEmail = fromEmail;
        log.info("Email service initialized. From: {}, Admin: {}", fromEmail, adminEmail);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void sendCustomerConfirmation(Booking booking) {
        try {
            // Re-fetch booking with user relationship in a new transaction
            Booking bookingWithUser = fetchBookingWithUser(booking.getId());
            if (bookingWithUser == null) {
                log.warn("Cannot send confirmation email: booking not found: {}", booking.getBookingRef());
                return;
            }
            
            String customerEmail = bookingWithUser.getUser().getEmail();
            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Cannot send confirmation email: customer email is missing for booking {}", 
                        booking.getBookingRef());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Booking Confirmation - " + bookingWithUser.getBookingRef());

            Context context = createBookingContext(bookingWithUser);
            String htmlContent = templateEngine.process("email/customer-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent confirmation email to customer {} for booking {}", 
                    customerEmail, bookingWithUser.getBookingRef());

        } catch (MessagingException e) {
            log.error("Failed to send customer confirmation email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending customer confirmation email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void sendAdminNotification(Booking booking) {
        try {
            if (adminEmail == null || adminEmail.isBlank()) {
                log.warn("Cannot send admin notification: admin email is not configured");
                return;
            }

            // Re-fetch booking with user relationship in a new transaction
            Booking bookingWithUser = fetchBookingWithUser(booking.getId());
            if (bookingWithUser == null) {
                log.warn("Cannot send admin notification: booking not found: {}", booking.getBookingRef());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("New Booking Received - " + bookingWithUser.getBookingRef());

            Context context = createBookingContext(bookingWithUser);
            String htmlContent = templateEngine.process("email/admin-notification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent admin notification email for booking {}", bookingWithUser.getBookingRef());

        } catch (MessagingException e) {
            log.error("Failed to send admin notification email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending admin notification email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void sendStatusUpdateEmail(Booking booking) {
        try {
            // Re-fetch booking with user relationship in a new transaction
            Booking bookingWithUser = fetchBookingWithUser(booking.getId());
            if (bookingWithUser == null) {
                log.warn("Cannot send status update email: booking not found: {}", booking.getBookingRef());
                return;
            }
            
            String customerEmail = bookingWithUser.getUser().getEmail();
            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Cannot send status update email: customer email is missing for booking {}", 
                        booking.getBookingRef());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Booking Status Update - " + bookingWithUser.getBookingRef());

            Context context = createBookingContext(bookingWithUser);
            String htmlContent = templateEngine.process("email/status-update", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent status update email to customer {} for booking {}", 
                    customerEmail, bookingWithUser.getBookingRef());

        } catch (MessagingException e) {
            log.error("Failed to send status update email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending status update email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        }
    }

    /**
     * Re-fetches a booking with its user relationship eagerly loaded.
     * This is necessary because the booking passed from the async call may have
     * lazy-loaded relationships that can't be accessed outside the original transaction.
     */
    private Booking fetchBookingWithUser(Long bookingId) {
        return bookingRepository.findByIdWithUser(bookingId).orElse(null);
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

