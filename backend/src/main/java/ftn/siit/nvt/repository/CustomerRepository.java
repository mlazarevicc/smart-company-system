package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}