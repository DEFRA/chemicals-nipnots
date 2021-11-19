package uk.gov.defra.reach.nipnots.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class ReachClientTest {

  private static final String LEGAL_ENTITY_JSON = "{\n"
          + "\"accountId\": \"53ebb2ba-acf3-4608-b9bf-d0bf780d6b55\",\n"
          + "\"name\": \"Legal Entity Name\",\n"
          + "\"postalCode\": \"AA1 1AA\"\n"
          + "}";

  private WireMockServer wireMockServer;

  private RestTemplate restTemplate;

  private ReachClient reachClient;

  @BeforeEach
  public void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + wireMockServer.port()));
    reachClient = new ReachClient(restTemplate);
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
    wireMockServer = null;
  }

  @Test
  void shouldRetrieveLegalEntityDetails() {
    UUID accountId = UUID.randomUUID();

    stubFor(get(urlEqualTo("/legal-entities/" + accountId))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(LEGAL_ENTITY_JSON)));

    LegalEntityDetails legalEntityDetails = reachClient.getLegalEntityDetails(accountId);

    assertThat(legalEntityDetails.getAccountId()).isEqualTo(UUID.fromString("53ebb2ba-acf3-4608-b9bf-d0bf780d6b55"));
    assertThat(legalEntityDetails.getName()).isEqualTo("Legal Entity Name");
    assertThat(legalEntityDetails.getPostalCode()).isEqualTo("AA1 1AA");
  }

}
