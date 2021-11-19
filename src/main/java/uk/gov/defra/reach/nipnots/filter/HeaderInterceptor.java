package uk.gov.defra.reach.nipnots.filter;

import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Adds JWT auth token and distributed logging headers to outgoing requests
 */
public class HeaderInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    HttpHeaders headers = request.getHeaders();
    headers.add("x-remote-user", MDC.get("userid"));
    headers.add("x-forwarded-for", MDC.get("x-forwarded-for"));
    headers.setBearerAuth((String) SecurityContextHolder.getContext().getAuthentication().getCredentials());

    return execution.execute(request, body);
  }
}
