package dio.budgeting.domain.dto;

import dio.budgeting.domain.Email;

public record AuthUserRequest(Email email, String password) {}
