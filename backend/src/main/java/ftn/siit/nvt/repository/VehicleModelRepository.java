package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    List<VehicleModel> findAllByMake_Id(Long makeId);
}
