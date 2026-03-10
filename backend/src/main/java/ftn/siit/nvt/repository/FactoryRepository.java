package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Factory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {

    Optional<Factory> findByName(String name);

    @Query(
            value = """
            SELECT f.*
            FROM factories f
            WHERE (:countryId IS NULL OR f.country_id = :countryId)
              AND (:cityId IS NULL OR f.city_id = :cityId)
              AND (:online IS NULL OR f.is_online = :online)
              AND (
                    :q IS NULL 
                 OR :q = '' 
                 OR f.search_vector @@ plainto_tsquery('simple', :q)
              )
            """,
                    countQuery = """
            SELECT COUNT(f.id)
            FROM factories f
            WHERE (:countryId IS NULL OR f.country_id = :countryId)
              AND (:cityId IS NULL OR f.city_id = :cityId)
              AND (:online IS NULL OR f.is_online = :online)
              AND (
                    :q IS NULL 
                 OR :q = '' 
                 OR f.search_vector @@ plainto_tsquery('simple', :q)
              )
            """,
            nativeQuery = true
    )
    Page<Factory> searchAdvanced(
            @Param("q") String q,
            @Param("countryId") Long countryId,
            @Param("cityId") Long cityId,
            @Param("online") Boolean online,
            Pageable pageable
    );

    List<Factory> findByIsOnlineFalse();

    @Query("SELECT DISTINCT f FROM Factory f " +
            "LEFT JOIN FETCH f.products " +
            "WHERE f.id BETWEEN :startId AND :endId " +
            "ORDER BY f.id")
    List<Factory> findByIdBetween(@Param("startId") Long startId,
                                  @Param("endId") Long endId);
}
