package uk.gov.defra.reach.nipnots.client;

import static uk.gov.defra.reach.security.Role.INDUSTRY_USER;
import static uk.gov.defra.reach.security.Role.REACH_MANAGER;
import static uk.gov.defra.reach.security.Role.REACH_READER;

import java.time.Instant;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.reach.monitoring.model.MonitoringEvent;
import uk.gov.defra.reach.monitoring.model.MonitoringEventDetails;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.security.AuthenticatedUser;

@Slf4j
@Component
public class MonitoringClient {
  private static final String COMPONENT = "reach-nipnots-service";
  private final RestTemplate monitoringRestTemplate;

  @Autowired
  public MonitoringClient(RestTemplate monitoringRestTemplate) {
    this.monitoringRestTemplate = monitoringRestTemplate;

  }

  public void sendNewNipNotsSpreadsheetEvent(Notification notification) {
    AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String userId = authenticatedUser.getUser().getUserId().toString();
    MonitoringEvent monitoringEvent = MonitoringEvent.builder()
         .sessionId(UUID.fromString(userId))
        .userId(getUserId(authenticatedUser))
        .dateTime(Instant.now())
        .pmcCode("0201")
        .component(COMPONENT)
        .priority(0)
        .details(MonitoringEventDetails.builder()
          .additionalInfo("")
          .message("Sheet " + notification.getFileName() + " processed for " + userId)
          .transactionCode("CHEM-SHEET-PROCESSED").build())
        .build();

    try {
      monitoringRestTemplate.postForEntity("/event", monitoringEvent, Void.class);
    } catch (Exception e) {
      log.error("Error sending monitoring event", e);
    }
  }

  private static String getUserId(AuthenticatedUser user) {
    if (user.getRole().equals(INDUSTRY_USER) || user.getRole().equals(REACH_MANAGER) || user.getRole().equals(REACH_READER)) {
      return "IDM/" + user.getUser().getUserId();
    } else {
      return "AAD/" + user.getUser().getUserId();
    }
  }
}
