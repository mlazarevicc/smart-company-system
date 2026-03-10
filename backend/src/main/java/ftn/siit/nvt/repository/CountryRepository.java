package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findAllByOrderByNameAsc();
}
