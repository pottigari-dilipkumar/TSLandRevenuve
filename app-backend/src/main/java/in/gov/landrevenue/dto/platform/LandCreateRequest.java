package in.gov.landrevenue.dto.platform;

import in.gov.landrevenue.model.platform.GeoPoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LandCreateRequest(
        @NotBlank String landId,
        @NotBlank String village,
        @NotBlank String surveyNumber,
        @NotBlank String seller,
        @NotBlank String buyer,
        @NotEmpty List<GeoPoint> polygon,
        @NotBlank String actor
) {
}
