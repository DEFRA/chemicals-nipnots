package uk.gov.defra.reach.nipnots.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;

@Slf4j
@Component
public class NotifyClient {
  private final RestTemplate notifyRestTemplate;

  public NotifyClient(RestTemplate notifyRestTemplate) {
    this.notifyRestTemplate = notifyRestTemplate;
  }

  public HttpStatus sendNotification(EmailNotification notification) {
    try {
      ResponseEntity<Void> notifyResponse = notifyRestTemplate.postForEntity("/email", notification, Void.class);
      return notifyResponse.getStatusCode();
    } catch (Exception e) {
      log.error("Error sending notification", e);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
