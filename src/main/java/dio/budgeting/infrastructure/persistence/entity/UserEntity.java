package dio.budgeting.infrastructure.persistence.entity;

import java.util.UUID;

import dio.budgeting.domain.User;
import dio.budgeting.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    public static UserEntity from(User user) {
        return new UserEntity(
                user.getId().uuid(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash());
    }

    public User toDomain() {
        return new User(
                new UserId(this.id),
                this.name,
                this.email,
                this.passwordHash);
    }
}
