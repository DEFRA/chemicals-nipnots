package uk.gov.defra.reach.nipnots.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.reach.file.Container;
import uk.gov.defra.reach.file.SerializableChecksum;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;
import uk.gov.defra.reach.nipnots.dto.NipNotResponse;

/**
 * End-to-end test designed to run against a live reach-nipnots instance complete with working dependencies.
 */
@TestMethodOrder(OrderAnnotation.class)
public class NotificationE2E {

  /**
   * JWT for industry1@email.com valid until December 2024.
   */
  private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJhYWFhYWFhYS0wMDAwLTAwMDEtZmZmZi1mZmZmZmZmZmZmZmYiLCJjb250YWN0SWQiOiJjY2NjY2NjYy0wMDAwLTAwMDEtZmZmZi1mZmZmZmZmZmZmZmYiLCJsZWdhbEVudGl0eUlkIjpudWxsLCJhY2NvdW50SWQiOiI2MWM0NTA0ZC1lODliLTEyZDMtYTQ1Ni0xMTExMTExMTExMTEiLCJsZWdhbEVudGl0eSI6IlJpY2htb25kIENoZW1pY2FscyIsImNvbXBhbnlUeXBlIjoiTGltaXRlZCBjb21wYW55IiwibGVnYWxFbnRpdHlSb2xlIjoiUkVBQ0ggTWFuYWdlciIsImdyb3VwcyI6bnVsbCwic291cmNlIjoiQjJDIiwicm9sZSI6IklORFVTVFJZX1VTRVIiLCJlbWFpbCI6ImluZHVzdHJ5MUBlbWFpbC5jb20iLCJpYXQiOjE2MDg2NDIzMDksImV4cCI6MTY3MTc1NzUwOX0.rjuZZ9c5EbTdrYkdHRF0JsOKfZy019no2LAEM2QEtIo";

  private static final String FILE_SERVICE_URL = System.getProperty("FILE_SERVICE_URL", "http://localhost:8090");

  private static final String NIPNOTS_SERVICE_URL = System.getProperty("NIPNOTS_SERVICE_URL", "http://localhost:8100");

  private static final RestTemplate REST_TEMPLATE = new RestTemplate();

  @Test
  @Order(1)
  void shouldSubmitNotification() {
    UUID storageLocation = UUID.randomUUID();
    Resource resource = getResource("classpath:spreadsheets/nipnots.xlsx");
    storeFile(resource, storageLocation);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(JWT_TOKEN);
    HttpEntity<NipNotRequest> request = new HttpEntity<>(new NipNotRequest(storageLocation, "nipnots.xlsx"), headers);

    NipNotResponse response = REST_TEMPLATE.postForObject(NIPNOTS_SERVICE_URL + "/notification", request, NipNotResponse.class);
    assertThat(response).isNotNull();
    assertThat(response.getFileName()).isEqualTo("nipnots.xlsx");
    assertThat(response.getNumberOfSubstances()).isEqualTo(1);
  }

  @Test
  @Order(2)
  void shouldGetLatestNotification() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(JWT_TOKEN);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<NipNotResponse> response = REST_TEMPLATE
        .exchange(NIPNOTS_SERVICE_URL + "/notification/latest", HttpMethod.GET, request, NipNotResponse.class);

    assertThat(Objects.requireNonNull(response.getBody()).getNumberOfSubstances()).isEqualTo(1);
  }

  private static void storeFile(Resource file, UUID target) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.setBearerAuth(JWT_TOKEN);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", file);
    body.add("container", Container.TEMPORARY.name());
    body.add("target", target.toString());
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<SerializableChecksum> response = REST_TEMPLATE.postForEntity(FILE_SERVICE_URL + "/file", requestEntity, SerializableChecksum.class);
    assertThat(Objects.requireNonNull(response.getBody()).get()).isNotNull();
  }

  @SneakyThrows
  private static Resource getResource(String fileName) {
    File file = ResourceUtils.getFile(fileName);
    return new FileSystemResource(file);
  }

}
