package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query(
            value = """
        SELECT p.* FROM products p
        WHERE p.is_deleted = false
          AND (:category IS NULL OR p.category = CAST(:category AS VARCHAR))
          AND (:available IS NULL OR p.is_available = :available)
          AND p.search_vector @@ plainto_tsquery('simple', :q)
        """,
            countQuery = """
        SELECT COUNT(p.id) FROM products p
        WHERE p.is_deleted = false
          AND (:category IS NULL OR p.category = CAST(:category AS VARCHAR))
          AND (:available IS NULL OR p.is_available = :available)
          AND p.search_vector @@ plainto_tsquery('simple', :q)
        """,
            nativeQuery = true
    )
    Page<Product> searchWithKeyword(
            @Param("q") String q,
            @Param("category") String category,
            @Param("available") Boolean available,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT p.* FROM products p
        WHERE p.is_deleted = false
          AND (:category IS NULL OR p.category = CAST(:category AS VARCHAR))
          AND (:available IS NULL OR p.is_available = :available)
        """,
            countQuery = """
        SELECT COUNT(p.id) FROM products p
        WHERE p.is_deleted = false
          AND (:category IS NULL OR p.category = CAST(:category AS VARCHAR))
          AND (:available IS NULL OR p.is_available = :available)
        """,
            nativeQuery = true
    )
    Page<Product> searchWithoutKeyword(
            @Param("category") String category,
            @Param("available") Boolean available,
            Pageable pageable
    );
}