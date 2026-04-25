package in.gov.landrevenue.clean.dto.registration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RegistrationResponse(
        Long id,
        String registrationRef,
        String status,

        // Property
        String propertyDistrict,
        String propertyVillage,
        String propertySurveyNumber,
        BigDecimal propertyAreaInAcres,
        BigDecimal marketValuePerAcre,
        BigDecimal totalMarketValue,
        BigDecimal considerationAmount,
        BigDecimal stampDuty,

        // Seller
        String sellerName,
        String sellerAadhaar,
        String sellerMobile,
        String sellerEmail,
        String sellerAddress,

        // Buyer
        String buyerName,
        String buyerAadhaar,
        String buyerMobile,
        String buyerEmail,
        String buyerAddress,

        // Workflow
        Long draftedByUserId,
        Long approvedByUserId,
        String rejectionReason,
        String notes,
        Instant createdAt,
        Instant submittedAt,
        Instant decidedAt,

        List<WitnessResponse> witnesses,
        List<DocumentResponse> documents
) {}
