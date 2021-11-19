package uk.gov.defra.reach.nipnots.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import lombok.SneakyThrows;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class HeaderInterceptorTest {

  private static final String JWT_TOKEN = "DUMMY JWT";

  private HeaderInterceptor headerInterceptor = new HeaderInterceptor();

  @Mock
  private ClientHttpRequestExecution execution;

  @Test
  @SneakyThrows
  void shouldSetHeaders() {
    mockAuthentication();
    mockMdc();
    ClientHttpRequest request = new SimpleClientHttpRequestFactory().createRequest(URI.create("http://somewhere.com"), HttpMethod.GET);
    headerInterceptor.intercept(request, new byte[0], execution);

    assertThat(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).containsExactly("Bearer DUMMY JWT");
    assertThat(request.getHeaders().get("x-remote-user")).containsExactly("user 1");
    assertThat(request.getHeaders().get("x-forwarded-for")).containsExactly("0.0.0.0");

    MDC.clear();
  }

  private static void mockAuthentication() {
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getCredentials()).thenReturn(JWT_TOKEN);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private static void mockMdc() {
    MDC.put("userid", "user 1");
    MDC.put("x-forwarded-for", "0.0.0.0");
  }

}
