package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.location.GeocodeRequest;
import ftn.siit.nvt.dto.location.GeocodeResponse;
import ftn.siit.nvt.dto.location.ReverseGeocodeRequest;
import ftn.siit.nvt.dto.location.ReverseGeocodeResponse;
import ftn.siit.nvt.service.GeocodingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('CUSTOMER')")
public class GeocodingController {

    private final GeocodingService geocodingService;

    @PostMapping("/factory-address")
    public ResponseEntity<GeocodeResponse> geocodeFactoryAddress(
            @RequestBody @Valid GeocodeRequest request
    ) {
        GeocodeResponse response = geocodingService.geocodeFactoryAddress(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reverse")
    public ResponseEntity<ReverseGeocodeResponse> reverseGeocode(
            @RequestBody @Valid ReverseGeocodeRequest request
    ) {
        ReverseGeocodeResponse response = geocodingService.reverseGeocode(request);
        return ResponseEntity.ok(response);
    }

}
