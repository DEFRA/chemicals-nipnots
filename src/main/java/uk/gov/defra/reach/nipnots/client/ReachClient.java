package uk.gov.defra.reach.nipnots.client;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ReachClient {

  private final RestTemplate reachRestTemplate;

  public ReachClient(RestTemplate reachRestTemplate) {
    this.reachRestTemplate = reachRestTemplate;
  }

  public LegalEntityDetails getLegalEntityDetails(UUID legalEntityAccountId) {
    return reachRestTemplate.getForObject("/legal-entities/{accountId}", LegalEntityDetails.class, legalEntityAccountId);
  }
}
