/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AadhaarOtpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "^[2-9][0-9]{11}$", message = "Aadhaar must be a valid 12-digit number")
        String aadhaarNumber,
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
        String otp
) {
}
