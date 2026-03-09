package in.gov.landrevenue.model.platform;

public record SystemUser(String username, String password, Role role, String email, String mobile) {
}
