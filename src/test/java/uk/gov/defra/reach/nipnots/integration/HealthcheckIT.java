package uk.gov.defra.reach.nipnots.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class HealthcheckIT {
  
  private static final String NIPNOTS_SERVICE_URL = System.getProperty("NIPNOTS_SERVICE_URL", "http://localhost:8100");

  private static final RestTemplate REST_TEMPLATE = new RestTemplate();

  @Test
  public void healthCheck_shouldReturn200_withoutAuthentication() {

    ResponseEntity<String> response = REST_TEMPLATE.exchange(NIPNOTS_SERVICE_URL + "/healthcheck", HttpMethod.GET, new HttpEntity<>(null, null), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void emptyEndpointForAzurePing_shouldReturn200_withoutAuthentication() {

    ResponseEntity<String> response = REST_TEMPLATE.exchange(NIPNOTS_SERVICE_URL + "/", HttpMethod.GET, new HttpEntity<>(null, null), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }


}
