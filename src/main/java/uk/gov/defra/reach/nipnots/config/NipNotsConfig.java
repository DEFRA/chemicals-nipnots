package uk.gov.defra.reach.nipnots.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class NipNotsConfig {

  @Value("${reach.nipnots.idPrefix}")
  private String idPrefix;

  @Value("${reach.nipnots.spreadsheet.expectedColumns}")
  private int expectedSpreadsheetColumnCount;

}
