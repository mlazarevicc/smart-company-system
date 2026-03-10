package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Company;
import ftn.siit.nvt.model.Customer;
import ftn.siit.nvt.model.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByOwnerId(Long ownerId);

    Page<Company> findAllByOwnerAndStatusIn(Customer owner, Collection<CompanyStatus> statuses, Pageable pageable);

    List<Company> findAllByOwnerAndStatus(Customer owner, CompanyStatus status);

    Optional<Object> findByName(String name);

    @Query(
            value = """
        SELECT c.*
        FROM companies c
        WHERE (:countryId IS NULL OR c.country_id = :countryId)
          AND (:cityId IS NULL OR c.city_id = :cityId)
          AND (:ownerId IS NULL OR c.customer_id = :ownerId)
          AND c.status IN ('APPROVED', 'PENDING')
          AND (
                :q IS NULL
             OR :q = ''
             OR c.search_vector @@ plainto_tsquery('simple', :q)
             OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(c.address) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY c.name DESC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true
    )
    List<Company> searchAdvanced(
            @Param("q") String q,
            @Param("countryId") Long countryId,
            @Param("cityId") Long cityId,
            @Param("ownerId") Long ownerId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(
            value = """
        SELECT COUNT(*)
        FROM companies c
        WHERE (:countryId IS NULL OR c.country_id = :countryId)
          AND (:cityId IS NULL OR c.city_id = :cityId)
          AND (:ownerId IS NULL OR c.customer_id = :ownerId)
          AND c.status IN ('APPROVED', 'PENDING')
          AND (
                :q IS NULL
             OR :q = ''
             OR c.search_vector @@ plainto_tsquery('simple', :q)
             OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(c.address) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        """,
            nativeQuery = true
    )
    long countAdvanced(
            @Param("q") String q,
            @Param("countryId") Long countryId,
            @Param("cityId") Long cityId,
            @Param("ownerId") Long ownerId
    );

    Page<Company> findAllByStatus(CompanyStatus status, Pageable pageable);
}
