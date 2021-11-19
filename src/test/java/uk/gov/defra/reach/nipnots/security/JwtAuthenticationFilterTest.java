package uk.gov.defra.reach.nipnots.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.defra.reach.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJhYWFhYWFhYS0wMDAwLTAwMDEtZmZmZi1mZmZmZmZmZmZmZmYiLCJjb250YWN0SWQiOiJjY2NjY2NjYy0wMDAwLTAwMDEtZmZmZi1mZmZmZmZmZmZmZmYiLCJsZWdhbEVudGl0eUlkIjpudWxsLCJhY2NvdW50SWQiOiI2MWM0NTA0ZC1lODliLTEyZDMtYTQ1Ni0xMTExMTExMTExMTEiLCJsZWdhbEVudGl0eSI6IlJpY2htb25kIENoZW1pY2FscyIsImNvbXBhbnlUeXBlIjoiTGltaXRlZCBjb21wYW55IiwibGVnYWxFbnRpdHlSb2xlIjoiUkVBQ0ggTWFuYWdlciIsImdyb3VwcyI6bnVsbCwic291cmNlIjoiQjJDIiwicm9sZSI6IklORFVTVFJZX1VTRVIiLCJlbWFpbCI6ImluZHVzdHJ5MUBlbWFpbC5jb20iLCJpYXQiOjE2MDg2NDIzMDksImV4cCI6MTY3MTc1NzUwOX0.rjuZZ9c5EbTdrYkdHRF0JsOKfZy019no2LAEM2QEtIo";

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setup() {
    jwtAuthenticationFilter = new JwtAuthenticationFilter(new JwtTokenValidator("MySecretKey"));
  }

  @Test
  @SneakyThrows
  void shouldDoNothingIfNoJwtInRequest() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @SneakyThrows
  void shouldPopulateSecurityContextIfJwtTokenPresentInRequest() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    FilterChain filterChain = Mockito.mock(FilterChain.class);

    when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
    when(request.getHeader("Authorization")).thenReturn("Bearer " + JWT_TOKEN);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isInstanceOf(AuthenticatedUser.class);
    assertThat(SecurityContextHolder.getContext().getAuthentication().getCredentials()).isEqualTo(JWT_TOKEN);

    verify(filterChain).doFilter(request, response);
  }

}
