package dio.budgeting.application;

import java.time.Instant;
import java.util.Arrays;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import dio.budgeting.domain.UserRepository;
import dio.budgeting.domain.dto.AuthUserRequest;
import dio.budgeting.domain.dto.AuthUserResponse;

@Service
public class AuthCandidateUseCase {
    @Value("${security.token.secret}")
    private String secretKey;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public AuthCandidateUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(AuthUserRequest authUserRequest) throws AuthenticationException {
        var user = this.userRepository.findUserByEmail(authUserRequest.email()).orElseThrow(()->{
            throw new UsernameNotFoundException("Username/password incoret");
        });

        var passwordMatches = this.passwordEncoder.matches(authUserRequest, user.getPassword());

        if (!passwordMatches) {
            throw new AuthenticationException();
        }

        var expiresIn = Instant.now().plus(Duration.ofMinutes(10));

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        var token = JWT.create().withIssuer("javagas").withSubject(candidate.getId().toString())
        .withExpiresAt(expiresIn)
        .withClaim("roles", Arrays.asList("candidate")).sign(algorithm);

        // var authCandidateResponse = AuthUserResponse;
        // .expires_in(expiresIn.toEpochMilli())
        // .build();

        // return authCandidateResponse;/
    }
}
