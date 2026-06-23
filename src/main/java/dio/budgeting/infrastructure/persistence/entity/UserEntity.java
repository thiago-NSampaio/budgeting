package dio.budgeting.infrastructure.persistence.entity;

import java.util.UUID;

import dio.budgeting.domain.User;
import dio.budgeting.domain.UserId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private UUID id;
    private String name;
    
    private String email;
    private String password;

    public static UserEntity from(User user){
        return new UserEntity(
            user.getId().uuid(),
            user.getName(),
            user.getEmail(),
            user.getPassword());
    }

    public User toDomain(){
        return new User(
            new UserId(this.id),
            this.name,
            this.email,
            this.password
        );
    }
}
