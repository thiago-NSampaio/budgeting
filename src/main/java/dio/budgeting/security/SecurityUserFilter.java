package dio.budgeting.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dio.budgeting.providers.JWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityUserFilter extends OncePerRequestFilter {

  private final JWTProvider jwtProvider;

  SecurityUserFilter(JWTProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith("Bearer ")) {

      var token = this.jwtProvider.validateToken(header);

      if (token == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(token.getSubject(), null);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }

}