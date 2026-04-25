package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAadhaarNumber(String aadhaarNumber);
}
