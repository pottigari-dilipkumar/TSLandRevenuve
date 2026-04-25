package in.gov.landrevenue.clean.dto.user;

import in.gov.landrevenue.clean.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotNull Role role,
        String fullName,
        String mobile,
        String email
) {}
