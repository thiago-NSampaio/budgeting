package dio.budgeting.domain;

public record Email(String value) {

    public Email {
        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
