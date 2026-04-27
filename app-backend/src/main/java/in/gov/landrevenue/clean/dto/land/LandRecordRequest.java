package in.gov.landrevenue.clean.dto.land;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LandRecordRequest(
        @NotBlank String surveyNumber,
        @NotBlank String district,
        @NotBlank String village,
        @NotNull @DecimalMin("0.01") BigDecimal areaInAcres,
        @NotNull Long ownerId,
        String landType,       // optional, defaults to PRIVATE
        String passbookNumber, // optional PPB number
        String geometry,       // GeoJSON polygon string
        String plusCode        // Open Location Code from centroid
) {}
