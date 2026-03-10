package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerUsernameOrderByOrderDateDesc(String username);
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.customer " +
            "JOIN FETCH o.deliveryCompany " +
            "JOIN FETCH o.items i " +
            "JOIN FETCH i.product " +
            "WHERE o.id = :orderId")
    Optional<Order> findOrderWithDetailsForInvoice(@Param("orderId") Long orderId);
}
