package ftn.siit.nvt.listener;

import ftn.siit.nvt.model.Order;
import ftn.siit.nvt.repository.OrderRepository;
import ftn.siit.nvt.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEmailListener {

    private final OrderRepository orderRepository;
    private final OrderNotificationService orderNotificationService;

    @RabbitListener(queues = "order-email-tasks")
    public void processOrderEmail(Long orderId) {
        try {
            Order savedOrder = orderRepository.findOrderWithDetailsForInvoice(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

            orderNotificationService.generateAndSendInvoice(savedOrder, savedOrder.getCustomer().getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send email for order " + orderId + ": " + e.getMessage());
        }
    }
}
