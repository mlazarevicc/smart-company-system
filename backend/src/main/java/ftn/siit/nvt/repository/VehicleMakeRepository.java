package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.VehicleMake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleMakeRepository extends JpaRepository<VehicleMake, Long> {
}
