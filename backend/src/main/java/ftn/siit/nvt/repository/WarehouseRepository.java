package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.model.Country;
import ftn.siit.nvt.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByName(String name);

    @Query(value = """
    SELECT w.* FROM warehouses w
    LEFT JOIN cities c ON w.city_id = c.id
    LEFT JOIN countries co ON c.country_id = co.id
    WHERE 
        (:search IS NULL OR w.search_vector @@ to_tsquery('simple', :search || ':*'))
        AND (:country IS NULL OR co.name = :country)
        AND (:isOnline IS NULL OR w.is_online = :isOnline)
    ORDER BY 
        CASE WHEN :search IS NOT NULL 
        THEN ts_rank(w.search_vector, to_tsquery('simple', :search || ':*')) 
        ELSE 0 END DESC,
        w.name ASC
    """,
            countQuery = """
    SELECT COUNT(*) FROM warehouses w
    LEFT JOIN cities c ON w.city_id = c.id
    LEFT JOIN countries co ON c.country_id = co.id
    WHERE 
        (:search IS NULL OR w.search_vector @@ to_tsquery('simple', :search || ':*'))
        AND (:country IS NULL OR co.name = :country)
        AND (:isOnline IS NULL OR w.is_online = :isOnline)
    """,
            nativeQuery = true)
    Page<Warehouse> findBySearchQueryAndFilters(
            @Param("search") String search,
            @Param("country") String country,
            @Param("isOnline") Boolean isOnline,
            Pageable pageable
    );

    @Query(value = """
    SELECT w.* FROM warehouses w
    LEFT JOIN cities c ON w.city_id = c.id
    LEFT JOIN countries co ON c.country_id = co.id
    WHERE 
        (:country IS NULL OR co.name = :country)
        AND (:isOnline IS NULL OR w.is_online = :isOnline)
    ORDER BY w.name ASC
    """,
            countQuery = """
    SELECT COUNT(*) FROM warehouses w
    LEFT JOIN cities c ON w.city_id = c.id
    LEFT JOIN countries co ON c.country_id = co.id
    WHERE 
        (:country IS NULL OR co.name = :country)
        AND (:isOnline IS NULL OR w.is_online = :isOnline)
    """,
            nativeQuery = true)
    Page<Warehouse> findByFilters(
            @Param("country") String country,
            @Param("isOnline") Boolean isOnline,
            Pageable pageable
    );
    @Query("SELECT w FROM Warehouse w WHERE w.isOnline = true AND w.lastHeartbeat < :cutoffTime")
    List<Warehouse> findTimedOutWarehouses(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<Warehouse> findByIsOnlineFalse();
    List<Warehouse> findByIdBetween(Long startId, Long endId);



}
