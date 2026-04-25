package in.gov.landrevenue.clean.dto.auth;

public record CitizenAuthResponse(
        String token,
        String role,
        boolean profileComplete,
        String fullName,
        String aadhaarNumber,
        String mobile,
        String email
) {}
