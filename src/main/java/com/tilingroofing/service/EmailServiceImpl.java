package com.tilingroofing.service;

import com.tilingroofing.domain.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final JavaMailSender mailSender;
    private final String adminEmail;
    private final String fromEmail;

    public EmailServiceImpl(
            TemplateEngine templateEngine,
            JavaMailSender mailSender,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${spring.mail.username:noreply@tilingroofing.com.au}") String fromEmail
    ) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
        this.fromEmail = fromEmail;
        log.info("Email service initialized. From: {}, Admin: {}", fromEmail, adminEmail);
    }

    @Override
    @Async
    public void sendCustomerConfirmation(Booking booking) {
        try {
            String customerEmail = booking.getUser().getEmail();
            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Cannot send confirmation email: customer email is missing for booking {}", 
                        booking.getBookingRef());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Booking Confirmation - " + booking.getBookingRef());

            Context context = createBookingContext(booking);
            String htmlContent = templateEngine.process("email/customer-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent confirmation email to customer {} for booking {}", 
                    customerEmail, booking.getBookingRef());

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
    public void sendAdminNotification(Booking booking) {
        try {
            if (adminEmail == null || adminEmail.isBlank()) {
                log.warn("Cannot send admin notification: admin email is not configured");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("New Booking Received - " + booking.getBookingRef());

            Context context = createBookingContext(booking);
            String htmlContent = templateEngine.process("email/admin-notification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent admin notification email for booking {}", booking.getBookingRef());

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
    public void sendStatusUpdateEmail(Booking booking) {
        try {
            String customerEmail = booking.getUser().getEmail();
            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Cannot send status update email: customer email is missing for booking {}", 
                        booking.getBookingRef());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("Booking Status Update - " + booking.getBookingRef());

            Context context = createBookingContext(booking);
            String htmlContent = templateEngine.process("email/status-update", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent status update email to customer {} for booking {}", 
                    customerEmail, booking.getBookingRef());

        } catch (MessagingException e) {
            log.error("Failed to send status update email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending status update email for booking {}: {}", 
                    booking.getBookingRef(), e.getMessage(), e);
        }
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

