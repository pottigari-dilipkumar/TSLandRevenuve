/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.repository;

import in.gov.landrevenue.model.RegistrationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRecordRepository extends JpaRepository<RegistrationRecordEntity, String> {
    boolean existsByRegistrationRef(String registrationRef);
}
