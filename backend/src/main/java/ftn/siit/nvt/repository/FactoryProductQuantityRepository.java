package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.FactoryProductQuantity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FactoryProductQuantityRepository extends JpaRepository<FactoryProductQuantity, Long> {

    @Query("SELECT COALESCE(SUM(fpq.quantity), 0) FROM FactoryProductQuantity fpq WHERE fpq.product.id = :productId")
    Long getTotalQuantityForProduct(@Param("productId") Long productId);

    @Query("SELECT fpq FROM FactoryProductQuantity fpq WHERE fpq.product.id = :productId AND fpq.quantity > 0 ORDER BY fpq.quantity DESC")
    List<FactoryProductQuantity> findAvailableQuantitiesForProduct(@Param("productId") Long productId);

    List<FactoryProductQuantity> findByProductIdIn(List<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FactoryProductQuantity f WHERE f.product.id IN :productIds")
    List<FactoryProductQuantity> findAvailableQuantitiesForProductsLocked(@Param("productIds") List<Long> productIds);
}
