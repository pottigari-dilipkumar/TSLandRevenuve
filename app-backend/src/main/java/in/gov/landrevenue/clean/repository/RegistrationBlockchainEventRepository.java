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

import in.gov.landrevenue.clean.entity.RegistrationBlockchainEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationBlockchainEventRepository extends JpaRepository<RegistrationBlockchainEvent, Long> {
    List<RegistrationBlockchainEvent> findByRegistrationRefOrderBySequenceAsc(String registrationRef);
    long countByRegistrationRef(String registrationRef);
}
