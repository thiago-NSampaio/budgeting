package dio.budgeting.domain.dto;

public record AuthUserResponse(String access_token, Long expires_in) {}