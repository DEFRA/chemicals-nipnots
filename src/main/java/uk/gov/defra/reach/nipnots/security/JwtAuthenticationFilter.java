package uk.gov.defra.reach.nipnots.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.jwt.JwtClaimReader;
import uk.gov.defra.reach.security.jwt.JwtMapper;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenValidator jwtTokenValidator;

  @Autowired
  public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      Optional<String> jwt = getJwtFromRequest(request);
      jwt.map(jwtTokenValidator::validateToken)
          .map(jwtContext -> {
            AuthenticatedUser user = JwtMapper.userFrom(new JwtClaimReader(jwtContext.getJwtClaims()));
            return new UsernamePasswordAuthenticationToken(user, jwt.get(), List.of());
          })
          .ifPresent(SecurityContextHolder.getContext()::setAuthentication);
    } catch (Exception ex) {
      log.error("Could not set user authentication in security context", ex);
    }

    filterChain.doFilter(request, response);
  }

  private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return Optional.of(bearerToken.substring(7));
    }
    return Optional.empty();
  }
}
