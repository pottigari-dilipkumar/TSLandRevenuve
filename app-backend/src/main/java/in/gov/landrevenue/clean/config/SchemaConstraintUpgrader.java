package in.gov.landrevenue.clean.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Drops and recreates the users.role check constraint so new roles
 * (SRO, SRO_ASSISTANT) are accepted. Hibernate ddl-auto:update does not
 * update existing check constraints when enum values change.
 */
@Component
@Order(Integer.MIN_VALUE)
public class SchemaConstraintUpgrader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaConstraintUpgrader.class);
    private final JdbcTemplate jdbcTemplate;

    public SchemaConstraintUpgrader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbcTemplate.execute(
                "ALTER TABLE users ADD CONSTRAINT users_role_check " +
                "CHECK (role IN ('ADMIN','REVENUE_OFFICER','DATA_ENTRY','CITIZEN','SRO','SRO_ASSISTANT'))"
            );
            log.info("users_role_check constraint updated with all roles");
        } catch (Exception e) {
            log.warn("Could not update users_role_check constraint: {}", e.getMessage());
        }
    }
}
