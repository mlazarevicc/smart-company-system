package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.factory.CityDTO;
import ftn.siit.nvt.dto.location.CityCountrySearchDTO;
import ftn.siit.nvt.model.City;
import ftn.siit.nvt.model.Country;
import ftn.siit.nvt.repository.CityRepository;
import ftn.siit.nvt.repository.CountryRepository;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityService {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @Cacheable(value = "cities_by_country", key = "#countryId")
    public List<CityDTO> getCitiesByCountry(Long countryId) {
        log.info("Fetching cities for country: {}", countryId);
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", countryId));

        return cityRepository.findByCountryIdOrderByNameAsc(country.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CityCountrySearchDTO> searchCityCountry(String query, int limit) {
        if (limit <= 0 || limit > 20) {
            limit = 10;
        }
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            // vraćamo prazan niz da ne povlačimo sve gradove
            return List.of();
        }

        var pageable = PageRequest.of(0, limit);
        List<City> cities = cityRepository.searchCityCountry(q, pageable);

        return cities.stream()
                .map(c -> new CityCountrySearchDTO(
                        c.getId(),
                        c.getName(),
                        c.getCountry().getId(),
                        c.getCountry().getName(),
                        c.getCountry().getCode(),
                        c.getLatitude(),
                        c.getLongitude()
                ))
                .toList();
    }

    private CityDTO convertToDTO(City city) {
        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setCountryId(city.getCountry().getId());
        dto.setCountryName(city.getCountry().getName());
        dto.setLatitude(city.getLatitude());
        dto.setLongitude(city.getLongitude());
        return dto;
    }
}
