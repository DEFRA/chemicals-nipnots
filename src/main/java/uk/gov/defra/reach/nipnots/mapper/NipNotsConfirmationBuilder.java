package uk.gov.defra.reach.nipnots.mapper;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.nipnots.entity.Notification;

@Component
public class NipNotsConfirmationBuilder {

  @Value("${chemical.regulations.url}")
  private String chemicalRegulationUrl;

  private static final String NIP_NOTS_CONFIRMATION = "NIP_NOTS_UPLOAD_CONFIRMATION";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy").withZone(ZoneId.from(ZoneOffset.UTC));

  public EmailNotification create(String recipientEmailAddress, Notification data, String action) {
    Map<String, String> personalisation = Map.of(
        "submission_date", DATE_FORMAT.format(data.getCreatedAt()),
        "notification_number", data.getReferenceNumber(),
        "filename", data.getFileName(),
        "number_of_substances", String.valueOf(data.getSubstances().size()),
        "reach_url", chemicalRegulationUrl,
        "action", action
    );

    return new EmailNotification(NIP_NOTS_CONFIRMATION, recipientEmailAddress, personalisation);
  }
}
