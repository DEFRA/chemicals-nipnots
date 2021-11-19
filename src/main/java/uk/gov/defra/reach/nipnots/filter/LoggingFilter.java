package uk.gov.defra.reach.nipnots.filter;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {
  private static final String USER_ID_KEY = "userid";
  private static final String X_REMOTE_USER = "x-remote-user";
  private static final String OPERATION_ID = "operation-id";
  private static final String SOURCE_IP = "x-forwarded-for";

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    registerIntoMDC(USER_ID_KEY, httpServletRequest.getHeader(X_REMOTE_USER));
    getRequestTelemetryContext().ifPresent(context -> registerIntoMDC(OPERATION_ID, context.getHttpRequestTelemetry().getContext().getOperation().getId()));
    registerIntoMDC(SOURCE_IP, httpServletRequest.getHeader(SOURCE_IP));
    try {
      registerUser(MDC.get(USER_ID_KEY));
      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      MDC.clear();
    }
  }

  private void registerUser(String userId) {
    getRequestTelemetryContext().ifPresent(context -> context.getHttpRequestTelemetry().getContext().getUser().setId(userId));
  }

  private void registerIntoMDC(String key, String value) {
    if (value != null && value.trim().length() > 0) {
      MDC.put(key, value);
    }
  }

  private static Optional<RequestTelemetryContext> getRequestTelemetryContext() {
    return Optional.ofNullable(ThreadContext.getRequestTelemetryContext());
  }

}
