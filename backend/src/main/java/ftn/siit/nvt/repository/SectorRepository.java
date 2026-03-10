package ftn.siit.nvt.repository;

import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByWarehouse(Warehouse warehouse);
    Optional<Sector> findByIdAndWarehouseId(Long id, Long warehouseId);
}
