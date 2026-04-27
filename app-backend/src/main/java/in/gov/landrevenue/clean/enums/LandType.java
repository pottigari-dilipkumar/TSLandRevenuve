package in.gov.landrevenue.clean.enums;

public enum LandType {
    PRIVATE,         // Normal privately-owned agricultural / residential land
    GOVERNMENT,      // Government-owned — cannot be transferred
    FOREST,          // Reserved forest land — cannot be transferred
    ASSIGNED,        // Government-assigned land (PPB holders) — restricted transfer
    INAM,            // Inam / grant land — restricted
    WAQF,            // Waqf board property
    NALA_CONVERTED   // Formerly agricultural, converted to non-agricultural (NALA)
}
