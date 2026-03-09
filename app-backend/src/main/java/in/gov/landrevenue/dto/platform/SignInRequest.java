package in.gov.landrevenue.dto.platform;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(@NotBlank String username, @NotBlank String password) {
}
