package uk.gov.defra.reach.nipnots.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NotifyActionParams {
  private final UUID contactId;
  private final EmailNotification notification;
}
