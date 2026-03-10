package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.DeliveryVehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryVehicleRepository extends JpaRepository<DeliveryVehicle, Long> {

    @Query(value = """
    SELECT v.* FROM vehicles v
    LEFT JOIN vehicle_makes m ON v.make_id = m.id
    LEFT JOIN vehicle_models mo ON v.model_id = mo.id
    WHERE
        v.search_vector @@ to_tsquery('simple', :q || ':*')
        AND (:makeId IS NULL OR m.id = :makeId)
        AND (:modelId IS NULL OR mo.id = :modelId)
    ORDER BY 
        ts_rank(v.search_vector, to_tsquery('simple', :q || ':*')) DESC,
        v.registration_number ASC
    """,
            countQuery = """
    SELECT COUNT(*) FROM vehicles v
    LEFT JOIN vehicle_makes m ON v.make_id = m.id
    LEFT JOIN vehicle_models mo ON v.model_id = mo.id
    WHERE
        v.search_vector @@ to_tsquery('simple', :q || ':*')
        AND (:makeId IS NULL OR m.id = :makeId)
        AND (:modelId IS NULL OR mo.id = :modelId)
    """,
            nativeQuery = true)
    Page<DeliveryVehicle> searchWithRank(
            @Param("q") String q,
            @Param("makeId") Long makeId,
            @Param("modelId") Long modelId,
            Pageable pageable
    );

    @Query("""
        SELECT v FROM DeliveryVehicle v
        LEFT JOIN v.make m
        LEFT JOIN v.model mo
        WHERE
            (:makeId IS NULL OR m.id = :makeId)
            AND (:modelId IS NULL OR mo.id = :modelId)
    """)
    Page<DeliveryVehicle> searchWithoutRank(
            @Param("makeId") Long makeId,
            @Param("modelId") Long modelId,
            Pageable pageable
    );

    Optional<DeliveryVehicle> findByRegistrationNumber(String registrationNumber);

    List<DeliveryVehicle> findByIdBetween(Long idAfter, Long idBefore);

    List<DeliveryVehicle> findByIsOnlineFalse();
}
