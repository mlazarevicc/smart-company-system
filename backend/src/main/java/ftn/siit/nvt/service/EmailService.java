package ftn.siit.nvt.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;

            String htmlMsg = String.format(
                    "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">" +
                            "    <div style=\"text-align: center; margin-bottom: 20px;\">" +
                            "        <h1 style=\"color: #005F73; margin: 0;\">SmartChain</h1>" +
                            "        <p style=\"color: #526071; font-size: 14px;\">Connected Logistics Platform</p>" +
                            "    </div>" +
                            "    <div style=\"background-color: #f8fafc; padding: 20px; border-radius: 8px; text-align: center;\">" +
                            "        <h2 style=\"color: #0A2540;\">Welcome to SmartChain!</h2>" +
                            "        <p style=\"color: #333; line-height: 1.6;\">Thank you for registering. Please activate your account to access the platform.</p>" +
                            "        <a href=\"%s\" style=\"display: inline-block; background-color: #00A8E8; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px;\">Verify My Account</a>" +
                            "    </div>" +
                            "    <div style=\"margin-top: 20px; text-align: center; font-size: 12px; color: #888;\">" +
                            "        <p>&copy; 2025 Smart Company Team. All rights reserved.</p>" +
                            "    </div>" +
                            "</div>",
                    verificationLink
            );

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject("Activate your SmartChain Account");
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            System.out.println("HTML Verification email sent to: " + toEmail);

        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
    @Async
    public void sendInvoiceEmail(String toEmail, byte[] pdfBytes, Long orderId) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            String htmlMsg = String.format(
                    "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">" +
                            "    <h2 style=\"color: #0A2540;\">Order successfully made!</h2>" +
                            "    <p>Respected user,</p>" +
                            "    <p>Your order #%d has been successfully made. We're sending you an invoice in the attachment.</p>" +
                            "    <p>Thank you!</p>" +
                            "    <p>SmartChain team</p>" +
                            "</div>", orderId);

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject("Invoice for order #" + orderId);
            helper.setFrom(fromEmail);

            helper.addAttachment("Invoice_" + orderId + ".pdf", new org.springframework.core.io.ByteArrayResource(pdfBytes));

            mailSender.send(mimeMessage);
            System.out.println("Invoice email sent to: " + toEmail);

        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send invoice email", e);
        }
    }

    @Async
    public void sendCompanyDecisionEmail(String toEmail, String reason, String companyName, String decision) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            String htmlMsg = String.format(
                    "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;\">" +
                            "    <h2 style=\"color: #0A2540;\">Decision regarding your company</h2>" +
                            "    <p>Respected user,</p>" +
                            "    <p>The creation request for your company %s has been %s. The manager in question gave the following reason:</p>" +
                            "    <b>%s</b>" +
                            "    <p>Thank you!</p>" +
                            "    <p>SmartChain team</p>" +
                            "</div>", companyName, decision, reason);

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject("Decision for the company " + companyName);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            System.out.println("Decision email sent to: " + toEmail);

        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send invoice email", e);
        }
    }
}