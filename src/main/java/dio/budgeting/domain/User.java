package dio.budgeting.domain;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class User {

    private final UserId id;
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User(UserId id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public User(String name, String email, String passwordHash) {
        this(new UserId(), name, email, passwordHash);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
