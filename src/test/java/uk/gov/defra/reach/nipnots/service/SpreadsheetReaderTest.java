package uk.gov.defra.reach.nipnots.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.base.Charsets;
import java.io.InputStream;

import java.io.UncheckedIOException;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.defra.reach.file.Container;

class SpreadsheetReaderTest {

  private WireMockServer wireMockServer;

  private SpreadsheetReader spreadsheetReader;

  @BeforeEach
  public void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + wireMockServer.port()));
    spreadsheetReader = new SpreadsheetReader(restTemplate);
  }

  @AfterEach
  public void stopWireMock() {
    wireMockServer.stop();
    wireMockServer = null;
  }

  @Test
  void shouldRetrieveSpreadsheetFile() {
    UUID storageLocation = UUID.randomUUID();

    String sasUri = "http://localhost:" + wireMockServer.port() + "/thefile/" + storageLocation;

    stubFor(get(urlPathEqualTo("/file"))
        .withQueryParam("container", equalTo(Container.TEMPORARY.name()))
        .withQueryParam("target", equalTo(storageLocation.toString()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"uri\": \"" + sasUri + "\"}")));

    stubFor(get(urlEqualTo("/thefile/" + storageLocation))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("the file contents".getBytes(Charsets.UTF_8))));

    InputStream inputStream = spreadsheetReader.retrieveSpreadsheet(storageLocation);

    assertThat(inputStream).hasBinaryContent("the file contents".getBytes(Charsets.UTF_8));
  }

  @Test
  void retrieveSpreadsheetFile_shouldThrowIOException() {
    UUID storageLocation = UUID.randomUUID();

    String sasUri = "http://fakehost:" + wireMockServer.port() + "/thefile/" + storageLocation;

    stubFor(get(urlPathEqualTo("/file"))
        .withQueryParam("container", equalTo(Container.TEMPORARY.name()))
        .withQueryParam("target", equalTo(storageLocation.toString()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"uri\": \"" + sasUri + "\"}")));

    assertThatExceptionOfType(UncheckedIOException.class).isThrownBy(() -> spreadsheetReader.retrieveSpreadsheet(storageLocation));
  }

  @Test
  void mapUriIfRequired_shouldTransformUri_whenNoOverridesAreSet() {
    URI originalUri = URI.create("http://originalhost:1234/thefile?query=abc");
    URI result = spreadsheetReader.mapUriIfRequired(originalUri);
    assertThat(result).hasHost("originalhost").hasPort(1234).hasPath("/thefile").hasQuery("query=abc");
  }

  @Test
  void mapUriIfRequired_shouldTransformUri_whenHostOverideSet() {
    URI originalUri = URI.create("http://originalhost:1234/thefile?query=abc");

    ReflectionTestUtils.setField(spreadsheetReader, "sasUriHostOverride", "newhost");
    URI result = spreadsheetReader.mapUriIfRequired(originalUri);
    assertThat(result).hasHost("newhost").hasPort(1234).hasPath("/thefile").hasQuery("query=abc");

  }

  @Test
  void mapUriIfRequired_shouldTransformUri_whenPortOverrideSet() {
    URI originalUri = URI.create("http://originalhost:1234/thefile?query=abc");

    ReflectionTestUtils.setField(spreadsheetReader, "sasUriPortOverride", 5678);
    URI result = spreadsheetReader.mapUriIfRequired(originalUri);
    assertThat(result).hasHost("originalhost").hasPort(5678).hasPath("/thefile").hasQuery("query=abc");
  }

  @Test
  void mapUriIfRequired_shouldTransformUri_whenPortAndHostOverrideSet() {
    URI originalUri = URI.create("http://originalhost:1234/thefile?query=abc");

    ReflectionTestUtils.setField(spreadsheetReader, "sasUriPortOverride", 5678);
    ReflectionTestUtils.setField(spreadsheetReader, "sasUriHostOverride", "newhost");
    URI result = spreadsheetReader.mapUriIfRequired(originalUri);
    assertThat(result).hasHost("newhost").hasPort(5678).hasPath("/thefile").hasQuery("query=abc");
  }

}
