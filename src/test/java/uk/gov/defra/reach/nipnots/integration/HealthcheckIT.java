package uk.gov.defra.reach.nipnots.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment =  WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-int.properties")
public class HealthcheckIT {

  private static String AUTHORIZATION_HEADER = "Authorization";
  private static String CONTENT_TYPE_HEADER = "Content-Type";
  private static String APPLICATION_JSON_HEADER_VALUE = "application/json";

  @Autowired
  private TestRestTemplate template;

  @Value("${reach.nipnots.jwt.secret}")
  private String testJwtToken;

  private HttpHeaders createAuthHeaders() {
    HttpHeaders authHeaders = new HttpHeaders();
    authHeaders.add(AUTHORIZATION_HEADER, testJwtToken);
    authHeaders.add(CONTENT_TYPE_HEADER, APPLICATION_JSON_HEADER_VALUE);
    return authHeaders;
  }


  @Test
  public void rootEndpoint_shouldReturn200() {
    ResponseEntity response = template.exchange("/", HttpMethod.GET, new HttpEntity<>(null, null), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("ok");
  }


}
