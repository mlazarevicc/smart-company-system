package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.vehicle.VehicleMakeDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.VehicleMake;
import ftn.siit.nvt.repository.VehicleMakeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleMakeService {

    private final VehicleMakeRepository vehicleMakeRepository;

    public VehicleMakeService(VehicleMakeRepository vehicleMakeRepository) {
        this.vehicleMakeRepository = vehicleMakeRepository;
    }

    public VehicleMakeDTO getMakeById(Long id) {
        VehicleMake vehicleMake = vehicleMakeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("VehicleMake", "id", id));
        return convertToDTO(vehicleMake);
    }

    @Cacheable(value = "all_makes")
    public List<VehicleMakeDTO> getAllMakes() {
        return vehicleMakeRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private VehicleMakeDTO convertToDTO(VehicleMake vehicleMake) {
        return new VehicleMakeDTO(vehicleMake);
    }
}
