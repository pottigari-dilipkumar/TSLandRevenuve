/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

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

        // Citizen checker-maker flow
        boolean initiatedByCitizen,
        Long sellerUserId,
        Long buyerUserId,
        Instant buyerApprovedAt,
        Long sroAssistantUserId,
        String revisionNotes,

        List<WitnessResponse> witnesses,
        List<DocumentResponse> documents,

        // Geo-location
        Double propertyLatitude,
        Double propertyLongitude,

        // Polygon boundary + PLUS Code
        String propertyGeometry,
        String propertyPlusCode,

        // Blockchain anchor
        String blockchainTxHash,
        Long blockchainBlockNumber,
        String blockchainSyncStatus,
        java.time.Instant blockchainSyncedAt
) {}
