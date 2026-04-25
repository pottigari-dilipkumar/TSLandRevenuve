package in.gov.landrevenue.clean.dto.registration;

public record WitnessResponse(
        Long id,
        String name,
        String aadhaarNumber,
        String mobile,
        String address,
        int sequenceNumber
) {}
