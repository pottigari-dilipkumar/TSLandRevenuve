/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.LandRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LandRecordRepository extends JpaRepository<LandRecord, Long>, JpaSpecificationExecutor<LandRecord> {
    Page<LandRecord> findAllByOwner_NationalId(String nationalId, Pageable pageable);
    Optional<LandRecord> findByIdAndOwner_NationalId(Long id, String nationalId);
    java.util.List<LandRecord> findByOwnerNationalId(String nationalId);
    java.util.Optional<LandRecord> findBySurveyNumber(String surveyNumber);
    java.util.List<LandRecord> findByDistrictIgnoreCaseAndVillageIgnoreCaseAndSurveyNumberIgnoreCase(
            String district, String village, String surveyNumber);
}
