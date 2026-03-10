package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    Optional<Manager> findByEmail(String email);
    Optional<Manager> findByUsername(String username);

    @Query(
            value = """
    SELECT m.* FROM manager m 
    WHERE m.username != 'admin'
      AND (:isBlocked IS NULL OR m.is_blocked = :isBlocked)
      AND m.search_vector @@ plainto_tsquery('simple', :q)
    """,
            countQuery = """
    SELECT COUNT(m.id) FROM manager m 
    WHERE m.username != 'admin'
      AND (:isBlocked IS NULL OR m.is_blocked = :isBlocked)
      AND m.search_vector @@ plainto_tsquery('simple', :q)
    """,
            nativeQuery = true
    )
    Page<Manager> searchWithKeyword(@Param("q") String q, @Param("isBlocked") Boolean isBlocked, Pageable pageable);

    @Query(
            value = """
    SELECT m.* FROM manager m 
    WHERE m.username != 'admin'
      AND (:isBlocked IS NULL OR m.is_blocked = :isBlocked)
    """,
            countQuery = """
    SELECT COUNT(m.id) FROM manager m 
    WHERE m.username != 'admin'
      AND (:isBlocked IS NULL OR m.is_blocked = :isBlocked)
    """,
            nativeQuery = true
    )
    Page<Manager> searchWithoutKeyword(@Param("isBlocked") Boolean isBlocked, Pageable pageable);
}
