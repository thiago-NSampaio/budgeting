package dio.budgeting.providers;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import dio.budgeting.domain.UserId;

@Component
public class AuthenticatedUserProvider {
    public UserId currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return new UserId(UUID.fromString(authentication.getName()));
    }
}