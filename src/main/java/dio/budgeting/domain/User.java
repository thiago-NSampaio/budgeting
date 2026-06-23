package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private UserId id;
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password){
        this.id = new UserId();
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
