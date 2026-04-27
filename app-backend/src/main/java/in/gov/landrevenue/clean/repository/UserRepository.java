package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.User;
import in.gov.landrevenue.clean.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByAadhaarNumber(String aadhaarNumber);
    Page<User> findByRoleIn(Collection<Role> roles, Pageable pageable);
}
