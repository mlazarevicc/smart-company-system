package ftn.siit.nvt.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ftn.siit.nvt.model.Order;
import ftn.siit.nvt.model.OrderItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    public byte[] generateInvoice(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Invoice for order #" + order.getId(), titleFont);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Customer: " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()));
            document.add(new Paragraph("Delivery to company: " + order.getDeliveryCompany().getName()));
            document.add(new Paragraph("Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Price per unit");
            table.addCell("Total");

            for (OrderItem item : order.getItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getPriceAtTimeOfPurchase().toString() + " EUR");
                table.addCell(item.getPriceAtTimeOfPurchase().multiply(java.math.BigDecimal.valueOf(item.getQuantity())).toString() + " EUR");
            }
            document.add(table);

            Paragraph total = new Paragraph("Total: " + order.getTotalPrice() + " EUR", titleFont);
            document.add(total);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error while generating invoice", e);
        }
    }
}
