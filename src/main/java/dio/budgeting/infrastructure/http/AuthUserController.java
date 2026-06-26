package dio.budgeting.infrastructure.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.AuthUserUseCase;
import dio.budgeting.domain.dto.AuthUserRequest;

@RestController
@RequestMapping("/auth")
public class AuthUserController {

  @Autowired
  private AuthUserUseCase authUserUseCase;

  @PostMapping("/auth")
  public ResponseEntity<Object> auth(@RequestBody AuthUserRequest authUserRequest) {
    try {
      var token = this.authUserUseCase.execute(authUserRequest);

      return ResponseEntity.ok().body(token);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
  }
}
