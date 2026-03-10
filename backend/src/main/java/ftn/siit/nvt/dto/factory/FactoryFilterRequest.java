package ftn.siit.nvt.dto.factory;

public record FactoryFilterRequest(
        String query,
        Long countryId,
        Long cityId,
        Boolean online
) {}
