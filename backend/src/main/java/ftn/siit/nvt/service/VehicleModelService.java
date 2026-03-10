package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.vehicle.VehicleModelDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.VehicleModel;
import ftn.siit.nvt.repository.VehicleModelRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public VehicleModelService(VehicleModelRepository vehicleModelRepository) {
        this.vehicleModelRepository = vehicleModelRepository;
    }

    @Cacheable(value = "all_models", key = "#id")
    public VehicleModelDTO getModelById(Long id) {
        VehicleModel vehicleModel = vehicleModelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("VehicleModel", "id", id));
        return convertToDTO(vehicleModel);
    }

    public List<VehicleModelDTO> getAllModelsByMakeId(Long makeId) {
        return vehicleModelRepository.findAllByMake_Id(makeId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private VehicleModelDTO convertToDTO(VehicleModel vehicleModel) {
        return new VehicleModelDTO(vehicleModel);
    }
}
