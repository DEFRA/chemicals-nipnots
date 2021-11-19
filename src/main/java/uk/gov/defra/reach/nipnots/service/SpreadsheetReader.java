package uk.gov.defra.reach.nipnots.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.defra.reach.file.Container;
import uk.gov.defra.reach.file.SerializableUri;


@Component
public class SpreadsheetReader {

  private static final String FILE_ENDPOINT = "/file?container={container}&target={target}";

  private final RestTemplate fileServiceRestTemplate;

  @Value("${azure.storage.sasUriHostOverride}")
  private String sasUriHostOverride;

  @Value("${azure.storage.sasUriPortOverride}")
  private Integer sasUriPortOverride;

  public SpreadsheetReader(RestTemplate fileServiceRestTemplate) {
    this.fileServiceRestTemplate = fileServiceRestTemplate;
  }

  /**
   * Retrieves a spreadsheet from Azure blob storage via the Reach File service
   *
   * @param storageLocation the storage location of the file
   * @return an inputstream of the file contents
   */
  public InputStream retrieveSpreadsheet(UUID storageLocation) {
    SerializableUri uriResponse = fileServiceRestTemplate.getForObject(FILE_ENDPOINT, SerializableUri.class, Container.TEMPORARY, storageLocation);

    if (uriResponse == null) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The uri response should not be empty!");
    }

    try {
      URL url = mapUriIfRequired(new URI(uriResponse.get())).toURL();
      return url.openConnection().getInputStream();
    } catch (IOException e) {
      throw new UncheckedIOException("Error retrieving spreadsheet file from blob storage", e);
    } catch (URISyntaxException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid uri found", e);
    }
  }

  URI mapUriIfRequired(URI uri) {
    if (sasUriHostOverride != null || sasUriPortOverride != null) {
      UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(uri);
      if (sasUriHostOverride != null) {
        uriBuilder.host(sasUriHostOverride);
      }
      if (sasUriPortOverride != null) {
        uriBuilder.port(sasUriPortOverride);
      }
      return uriBuilder.build().toUri();
    } else {
      return uri;
    }
  }

}
