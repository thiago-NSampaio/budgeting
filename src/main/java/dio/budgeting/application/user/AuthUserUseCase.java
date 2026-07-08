package dio.budgeting.application.user;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import dio.budgeting.domain.dto.AuthUserRequest;
import dio.budgeting.domain.dto.AuthUserResponse;
import dio.budgeting.domain.user.UserRepository;

@Service
public class AuthUserUseCase {
    @Value("${security.token.secret}")
    private String secretKey;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public AuthUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthUserResponse execute(AuthUserRequest authUserRequest) {
        var user = userRepository.findByEmail(authUserRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(authUserRequest.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(10));
        String token = JWT.create()
                .withIssuer("aura-finance")
                .withSubject(user.getId().uuid().toString())
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey));

        var auth = new AuthUserResponse(
                token,
                Duration.ofMinutes(10).toSeconds());

        return auth;
    }
}
