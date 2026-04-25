package in.gov.landrevenue.clean.config;

import in.gov.landrevenue.clean.entity.MarketValue;
import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import in.gov.landrevenue.clean.repository.MarketValueRepository;
import in.gov.landrevenue.clean.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MarketValueRepository marketValueRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           MarketValueRepository marketValueRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.marketValueRepository = marketValueRepository;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin123", Role.ADMIN);
        seedUser("officer", "officer123", Role.REVENUE_OFFICER);
        seedUser("entry", "entry123", Role.DATA_ENTRY);
        seedUser("sro", "sro123", Role.SRO);
        seedUser("sro_asst", "sro_asst123", Role.SRO_ASSISTANT);
        seedMarketValues();
    }

    private void seedUser(String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }

    private void seedMarketValues() {
        if (marketValueRepository.count() > 0) {
            return;
        }
        seedMarketValue("Hyderabad", "Kukatpally", new BigDecimal("4500000"));
        seedMarketValue("Hyderabad", "Madhapur", new BigDecimal("6200000"));
        seedMarketValue("Hyderabad", "Gachibowli", new BigDecimal("5800000"));
        seedMarketValue("Rangareddy", "Shamshabad", new BigDecimal("2800000"));
        seedMarketValue("Rangareddy", "Narsingi", new BigDecimal("3500000"));
        seedMarketValue("Medchal", "Kompally", new BigDecimal("2200000"));
        seedMarketValue("Medchal", "Dundigal", new BigDecimal("1800000"));
        seedMarketValue("Nalgonda", "Miryalaguda", new BigDecimal("800000"));
        seedMarketValue("Nalgonda", "Suryapet", new BigDecimal("750000"));
        seedMarketValue("Warangal", "Hanamkonda", new BigDecimal("1200000"));
        seedMarketValue("Warangal", "Kazipet", new BigDecimal("1100000"));
        seedMarketValue("Karimnagar", "Jammikunta", new BigDecimal("950000"));
    }

    private void seedMarketValue(String district, String village, BigDecimal ratePerAcre) {
        MarketValue mv = new MarketValue();
        mv.setDistrict(district);
        mv.setVillage(village);
        mv.setRatePerAcre(ratePerAcre);
        mv.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        mv.setEffectiveTo(null);
        marketValueRepository.save(mv);
    }
}
