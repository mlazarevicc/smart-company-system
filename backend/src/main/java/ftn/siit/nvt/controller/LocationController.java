package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.factory.CountryDTO;
import ftn.siit.nvt.dto.factory.CityDTO;
import ftn.siit.nvt.dto.location.CityCountrySearchDTO;
import ftn.siit.nvt.service.CountryService;
import ftn.siit.nvt.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final CountryService countryService;
    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        List<CountryDTO> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryDTO> getCountryById(@PathVariable Long id) {
        CountryDTO country = countryService.getCountryById(id);
        return ResponseEntity.ok(country);
    }

    @GetMapping("/{id}/cities")
    public ResponseEntity<List<CityDTO>> getCitiesByCountry(@PathVariable Long id) {
        List<CityDTO> cities = cityService.getCitiesByCountry(id);
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CityCountrySearchDTO>> searchCityCountry(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<CityCountrySearchDTO> results = cityService.searchCityCountry(q, limit);
        return ResponseEntity.ok(results);
    }
}
