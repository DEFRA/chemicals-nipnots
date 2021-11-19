package uk.gov.defra.reach.nipnots.client;

import java.util.UUID;
import lombok.Data;

@Data
public class LegalEntityDetails {

  private UUID accountId;

  private String name;

  private String postalCode;

}
