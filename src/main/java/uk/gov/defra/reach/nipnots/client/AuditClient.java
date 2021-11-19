package uk.gov.defra.reach.nipnots.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.defra.reach.nipnots.dto.AuditEvent;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.nipnots.dto.NotifyActionParams;
import uk.gov.defra.reach.security.AuthenticatedUser;

@Component
public class AuditClient {

  private static final ObjectMapper JSON_MAPPER = Jackson2ObjectMapperBuilder.json().build();
  private final RestTemplate auditRestTemplate;

  public AuditClient(RestTemplate auditRestTemplate) {
    this.auditRestTemplate = auditRestTemplate;
  }

  public void auditNotificationEvent(AuthenticatedUser user, EmailNotification notification, int response) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    UUID contactId = user.getUser().getContactId().orElseThrow();
    NotifyActionParams notifyActionParams = new NotifyActionParams(contactId, notification);

    try {
      AuditEvent auditEvent = new AuditEvent("sendNotification", JSON_MAPPER.writeValueAsString(notifyActionParams),
          response, request.getHeader("x-forwarded-for"), user);
      auditRestTemplate.postForEntity("/audit", auditEvent, Void.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Cannot make JSON string", e);
    }
  }
}
