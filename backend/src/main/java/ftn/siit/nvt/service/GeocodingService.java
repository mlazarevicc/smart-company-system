package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.location.GeocodeRequest;
import ftn.siit.nvt.dto.location.GeocodeResponse;
import ftn.siit.nvt.dto.location.ReverseGeocodeRequest;
import ftn.siit.nvt.dto.location.ReverseGeocodeResponse;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.City;
import ftn.siit.nvt.model.Country;
import ftn.siit.nvt.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {

    private final CityRepository cityRepository;

    @Value("${geocoding.nominatim.base-url}")
    private String nominatimBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public GeocodeResponse geocodeFactoryAddress(GeocodeRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));

        String query = request.getStreetAddress() + ", " + city.getName() + ", " + city.getCountry().getCode();
        String street = request.getStreetAddress();
        String cityName = city.getName();
        String countryName = city.getCountry().getName();

        String url = UriComponentsBuilder.fromHttpUrl(nominatimBaseUrl + "/search")
                .queryParam("street", street)
                .queryParam("city", cityName)
                .queryParam("country", countryName)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .toUriString();


        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "NVT-Project-Geocoder/1.0");
        headers.set("Accept-Language", "en");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            System.out.println("URL: " + url);

            ResponseEntity<NominatimResult[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResult[].class
            );

            NominatimResult[] body = response.getBody();
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body length: " + (body == null ? "null" : body.length));

            if (body == null || body.length == 0) {
                throw new IllegalArgumentException("Could not geocode address. Please check the address and try again.");
            }

            NominatimResult result = body[0];
            Double lat = Double.valueOf(result.lat);
            Double lon = Double.valueOf(result.lon);

            String displayName = result.display_name != null ? result.display_name : query;

            return new GeocodeResponse(lat, lon, displayName);

        } catch (Exception e) {
            log.error("Failed to geocode address '{}': {}", query, e.getMessage(), e);
            throw new IllegalStateException("Failed to geocode address. Please try again later.");
        }
    }

    public ReverseGeocodeResponse reverseGeocode(ReverseGeocodeRequest request) {
        String url = UriComponentsBuilder
                .fromHttpUrl(nominatimBaseUrl + "/reverse")
                .queryParam("lat", request.getLatitude())
                .queryParam("lon", request.getLongitude())
                .queryParam("format", "json")
                .queryParam("accept-language", "en")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "NVT-Project-Geocoder/1.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<NominatimReverseResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimReverseResult.class
            );

            NominatimReverseResult body = response.getBody();
            if (body == null || body.address == null) {
                throw new IllegalArgumentException("Could not reverse geocode coordinates.");
            }

            String street = body.address.road != null ? body.address.road : "";
            String house = body.address.house_number != null ? body.address.house_number : "";
            String streetAddress = (street + " " + house).trim();
            if (streetAddress.isEmpty()) {
                streetAddress = body.display_name != null ? body.display_name : "";
            }

            String cityName = body.address.city != null
                    ? body.address.city
                    : (body.address.town != null
                    ? body.address.town
                    : body.address.village);

            String countryCode = body.address.country_code;
            String countryNameFromOSM = body.address.country;

            City city = null;
            Country country = null;

            System.out.println("PRINT 1: " + cityName + " " + countryCode);

            if (cityName != null && countryCode != null) {
                city = cityRepository
                        .findByNameIgnoreCaseAndCountry_CodeIgnoreCase(cityName, countryCode)
                        .orElse(null);
                if (city != null) {
                    country = city.getCountry();
                }
            }

            if (city == null || country == null) {
                return new ReverseGeocodeResponse(
                        streetAddress,
                        cityName,
                        countryNameFromOSM,
                        countryCode,
                        null,
                        null
                );
            }

            return new ReverseGeocodeResponse(
                    streetAddress,
                    city.getName(),
                    country.getName(),
                    country.getCode(),
                    city.getId(),
                    country.getId()
            );
        } catch (Exception e) {
            log.error("Failed to reverse geocode lat={}, lon={}: {}",
                    request.getLatitude(), request.getLongitude(), e.getMessage(), e);
            throw new IllegalStateException("Failed to reverse geocode location.");
        }
    }


    private static class NominatimReverseResult {
        public String display_name;
        public Address address;

        public static class Address {
            public String road;
            public String house_number;
            public String city;
            public String town;
            public String village;
            public String country;
            public String country_code;
        }
    }



    private static class NominatimResult {
        public String lat;
        public String lon;
        public String display_name;
    }

}
