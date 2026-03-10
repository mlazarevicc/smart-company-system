package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.City;
import ftn.siit.nvt.model.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountry(Country country);
    List<City> findByCountryIdOrderByNameAsc(Long countryId);

    @Query("""
           SELECT c
           FROM City c
           JOIN c.country co
           WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(co.name) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(co.code) LIKE LOWER(CONCAT('%', :q, '%'))
           ORDER BY c.name ASC
           """)
    List<City> searchCityCountry(String q, Pageable pageable);

    Optional<City> findByNameIgnoreCaseAndCountry_CodeIgnoreCase(String name, String countryCode);
}
