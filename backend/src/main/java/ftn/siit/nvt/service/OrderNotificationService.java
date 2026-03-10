package ftn.siit.nvt.service;

import ftn.siit.nvt.model.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {
    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;

    public OrderNotificationService(InvoicePdfService invoicePdfService, EmailService emailService) {
        this.invoicePdfService = invoicePdfService;
        this.emailService = emailService;
    }

    @Async
    public void generateAndSendInvoice(Order savedOrder, String email) {
        byte[] pdfBytes = invoicePdfService.generateInvoice(savedOrder);
        emailService.sendInvoiceEmail(email, pdfBytes, savedOrder.getId());
    }
}
