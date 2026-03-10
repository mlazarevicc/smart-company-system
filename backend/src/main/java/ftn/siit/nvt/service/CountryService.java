package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.factory.CountryDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Country;
import ftn.siit.nvt.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    @Cacheable(value = "all_countries")
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "country", key = "#id")
    public CountryDTO getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id));
        return convertToDTO(country);
    }

    private CountryDTO convertToDTO(Country country) {
        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCode(country.getCode());
        return dto;
    }
}