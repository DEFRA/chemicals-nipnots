package uk.gov.defra.reach.nipnots.dto;

import java.util.Map;
import lombok.Data;

@Data
public class EmailNotification {

  private final String event;

  private final String emailAddress;

  private final Map<String, String> personalisation;

}
