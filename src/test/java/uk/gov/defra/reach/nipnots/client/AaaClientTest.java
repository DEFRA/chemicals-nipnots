package uk.gov.defra.reach.nipnots.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.LegalEntity;
import uk.gov.defra.reach.security.Role;
import uk.gov.defra.reach.security.User;

class AaaClientTest {

  private static final String LEGAL_ENTITY_JSON = "{\n"
      + "\"accountId\": \"53ebb2ba-acf3-4608-b9bf-d0bf780d6b55\",\n"
      + "\"name\": \"Legal Entity Name\",\n"
      + "\"postalCode\": \"AA1 1AA\"\n"
      + "}";

  private WireMockServer wireMockServer;

  private RestTemplate restTemplate;

  private AuditClient auditClient;

  @BeforeEach
  public void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + wireMockServer.port()));
    auditClient = new AuditClient(restTemplate);
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
    wireMockServer = null;
  }

  @Test
  void shouldAuditNotificationEventSuccessfully() {
    UUID contactId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    AuthenticatedUser user = new AuthenticatedUser(User.from("test-source", id, Optional.of(contactId)), new LegalEntity(UUID.randomUUID(), UUID.randomUUID(), "test-account"), Role.REACH_MANAGER);
    mockRequestContext();

    EmailNotification request = new EmailNotification("test-event", "test@email.com", Map.of("test-event", "event1"));
    HttpStatus responseCode = HttpStatus.CREATED;

    stubFor(post(urlEqualTo("/audit")).withHeader("Content-Type", equalTo("application/json")).willReturn(aResponse().withStatus(202)));

    auditClient.auditNotificationEvent(user, request, responseCode.value());

    WireMock.verify(postRequestedFor(urlEqualTo("/audit")));
  }

  private static void mockRequestContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }
}
