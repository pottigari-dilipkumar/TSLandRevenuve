package in.gov.landrevenue.clean.dto.user;

public record UserResponse(
        Long id,
        String username,
        String role,
        String fullName,
        String mobile,
        String email
) {}
