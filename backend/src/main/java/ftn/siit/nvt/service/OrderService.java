package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.order.CreateOrderRequest;
import ftn.siit.nvt.dto.order.OrderHistoryResponse;
import ftn.siit.nvt.dto.order.OrderItemRequest;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.*;
import ftn.siit.nvt.repository.*;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FactoryProductQuantityRepository fpqRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final OrderNotificationService orderNotificationService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        FactoryProductQuantityRepository fpqRepository, CompanyRepository companyRepository,
                        CustomerRepository customerRepository, OrderNotificationService orderNotificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.fpqRepository = fpqRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.orderNotificationService = orderNotificationService;
    }

    @Transactional(
            rollbackFor = Exception.class,
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED
    )
    public Long createOrder(CreateOrderRequest request, String customerUsername) {
        Customer customer = customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "username", customerUsername));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryCompany(company);
        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        List<Long> productIds = request.getItems().stream().map(OrderItemRequest::getProductId).distinct().sorted().toList();

        List<Product> products = productRepository.findAllById(productIds);

        List<FactoryProductQuantity> allAvailableQuantities = fpqRepository.findByProductIdIn(productIds);

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(itemReq.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            List<FactoryProductQuantity> productFpqs = allAvailableQuantities.stream()
                    .filter(fpq -> fpq.getProduct().getId().equals(product.getId()))
                    .toList();

            long totalAvailable = productFpqs.stream().mapToLong(FactoryProductQuantity::getQuantity).sum();

            if (totalAvailable < itemReq.getQuantity()) {
                throw new IllegalStateException("Not enough supplies: " + product.getName() + ". Available: " + totalAvailable);
            }

            Long quantityToFulfill = itemReq.getQuantity();

            for (FactoryProductQuantity fpq : productFpqs) {
                if (quantityToFulfill <= 0) break;

                if (fpq.getQuantity() >= quantityToFulfill) {
                    fpq.setQuantity(fpq.getQuantity() - quantityToFulfill);
                    quantityToFulfill = 0L;
                } else {
                    quantityToFulfill -= fpq.getQuantity();
                    fpq.setQuantity(0L);
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPriceAtTimeOfPurchase(product.getPrice());
            order.getItems().add(orderItem);

            totalOrderPrice = totalOrderPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalPrice(totalOrderPrice);
        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getCustomerOrders(String username) {
        customerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "username", username));

        List<Order> orders = orderRepository.findByCustomerUsernameOrderByOrderDateDesc(username);

        return orders.stream().map(order -> {
            OrderHistoryResponse response = new OrderHistoryResponse();
            response.setId(order.getId());
            response.setOrderDate(order.getOrderDate());
            response.setDeliveryCompanyName(order.getDeliveryCompany().getName());
            response.setTotalPrice(order.getTotalPrice());

            List<OrderHistoryResponse.OrderItemHistory> items = order.getItems().stream().map(item -> {
                OrderHistoryResponse.OrderItemHistory itemHistory = new OrderHistoryResponse.OrderItemHistory();
                itemHistory.setProductName(item.getProduct().getName());
                itemHistory.setQuantity(item.getQuantity());
                itemHistory.setPrice(item.getPriceAtTimeOfPurchase());
                return itemHistory;
            }).collect(Collectors.toList());

            response.setItems(items);
            return response;
        }).collect(Collectors.toList());
    }
}
