package in.gov.landrevenue.clean.dto.registration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RegistrationDraftRequest(
        @NotBlank String propertyDistrict,
        @NotBlank String propertyVillage,
        @NotBlank String propertySurveyNumber,
        @NotNull @Positive BigDecimal propertyAreaInAcres,
        @NotNull @Positive BigDecimal considerationAmount,

        @NotNull @Valid RegistrationPartyDto seller,
        @NotNull @Valid RegistrationPartyDto buyer,

        @Size(max = 3) List<@Valid RegistrationWitnessDto> witnesses,

        String notes,

        // Optional geo-location captured from the browser at time of registration
        Double propertyLatitude,
        Double propertyLongitude,

        // Polygon boundary drawn on map (GeoJSON geometry string) + PLUS Code
        String propertyGeometry,
        String propertyPlusCode
) {}
