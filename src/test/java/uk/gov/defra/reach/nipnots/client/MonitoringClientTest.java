package uk.gov.defra.reach.nipnots.client;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.LegalEntity;
import uk.gov.defra.reach.security.Role;
import uk.gov.defra.reach.security.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MonitoringClientTest {
  private static final UUID LEGAL_ENTITY_ACCOUNT_ID = UUID.randomUUID();

  private Logger logger;

  private ListAppender<ILoggingEvent> listAppender;

  private WireMockServer wireMockServer;

  private RestTemplate restTemplate;

  private MonitoringClient monitoringClient;

  @BeforeEach
  public void startWireMock() {
    logger = (Logger) LoggerFactory.getLogger(MonitoringClient.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession fakeSession = new MockHttpSession(null, UUID.randomUUID().toString());
    request.setSession(fakeSession);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));


    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + wireMockServer.port()));
    monitoringClient = new MonitoringClient(restTemplate);
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
    wireMockServer = null;
  }

  @Test
  void shouldSendNewNipnotsSpreadsheetEvent() {
    mockSecurityPrincipal();
    Notification testNotification = Mockito.mock(Notification.class);
    stubFor(post(urlEqualTo("/event")).withHeader("Content-Type", equalTo("application/json")).willReturn(aResponse().withStatus(201)));
    monitoringClient.sendNewNipNotsSpreadsheetEvent(testNotification);

    WireMock.verify(postRequestedFor(urlEqualTo("/event")));
  }

  @Test
  void shouldLogIfMonitoringEventFails() {
    mockSecurityPrincipal();
    Notification testNotification = Mockito.mock(Notification.class);
    stubFor(post(urlEqualTo("/event")).withHeader("Content-Type", equalTo("application/json")).willReturn(aResponse().withStatus(500)));
    monitoringClient.sendNewNipNotsSpreadsheetEvent(testNotification);

    List<ILoggingEvent> logList = listAppender.list;

    WireMock.verify(postRequestedFor(urlEqualTo("/event")));
    assertThat(logList.get(0).getMessage()).isEqualTo("Error sending monitoring event");
  }

  private static void mockSecurityPrincipal() {
    UUID testUserId = UUID.randomUUID();
    Authentication authentication = Mockito.mock(Authentication.class);
    User user = User.from("test-source", testUserId, Optional.empty());

    LegalEntity legalEntity = new LegalEntity();
    legalEntity.setAccountId(LEGAL_ENTITY_ACCOUNT_ID);

    AuthenticatedUser authenticatedUser = new AuthenticatedUser(user, legalEntity, Role.REACH_MANAGER);
    when(authentication.getPrincipal()).thenReturn(authenticatedUser);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
