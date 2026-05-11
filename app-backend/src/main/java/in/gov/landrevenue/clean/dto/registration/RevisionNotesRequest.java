package in.gov.landrevenue.clean.dto.registration;

import jakarta.validation.constraints.NotBlank;

public record RevisionNotesRequest(@NotBlank String notes) {}
