package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.order.CreateOrderRequest;
import ftn.siit.nvt.dto.order.OrderHistoryResponse;
import ftn.siit.nvt.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RabbitTemplate rabbitTemplate;

    public OrderController(OrderService orderService, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        String username = authentication.getName();
        Long orderId = orderService.createOrder(request, username);
        rabbitTemplate.convertAndSend("smart-manufacturing.orders", "order.created.email", orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order #" + orderId + " successfully created.");
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderHistoryResponse>> getMyOrders(Authentication authentication) {
        String username = authentication.getName();
        List<OrderHistoryResponse> history = orderService.getCustomerOrders(username);
        return ResponseEntity.ok(history);
    }
}
