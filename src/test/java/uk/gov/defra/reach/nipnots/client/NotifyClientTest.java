package uk.gov.defra.reach.nipnots.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;

class NotifyClientTest {

  private Logger logger;

  private ListAppender<ILoggingEvent> listAppender;

  private WireMockServer wireMockServer;

  private NotifyClient notifyClient;

  @BeforeEach
  public void startWireMock() {
    logger = (Logger) LoggerFactory.getLogger(NotifyClient.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + wireMockServer.port()));
    notifyClient = new NotifyClient(restTemplate);
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
    wireMockServer = null;
    listAppender.stop();
  }

  @Test
  void shouldAuditNotificationEventSuccessfully() {
    EmailNotification request = new EmailNotification("test-event", "test@email.com", Map.of("test-event", "event1"));
    HttpStatus responseCode = HttpStatus.CREATED;

    stubFor(post(urlEqualTo("/email")).withHeader("Content-Type", equalTo("application/json")).willReturn(aResponse().withStatus(201)));

    HttpStatus status = notifyClient.sendNotification(request);

    List<ILoggingEvent> logList = listAppender.list;

    WireMock.verify(postRequestedFor(urlEqualTo("/email")));
    assertThat(logList.size()).isEqualTo(0);
    assertThat(status.value()).isEqualTo(responseCode.value());
  }

  @Test
  void shouldLogIfAuditCallFails() {
    EmailNotification request = new EmailNotification("test-event", "test@email.com", Map.of("test-event", "event1"));
    HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;

    stubFor(post(urlEqualTo("/email")).withHeader("Content-Type", equalTo("application/json")).willReturn(aResponse().withStatus(500)));

    HttpStatus status = notifyClient.sendNotification(request);

    List<ILoggingEvent> logList = listAppender.list;

    WireMock.verify(postRequestedFor(urlEqualTo("/email")));
    assertThat(logList.get(0).getMessage()).isEqualTo("Error sending notification");
    assertThat(status.value()).isEqualTo(responseCode.value());
  }
}
