package uk.gov.defra.reach.nipnots.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.defra.reach.nipnots.service.NipnotSpreadsheetParserFactory;

@Configuration
public class SpreadsheetParserConfiguration {

  @Value("${reach.nipnots.spreadsheet.headerRowIndex}")
  private int headerRowIndex;

  @Value("${reach.nipnots.spreadsheet.exampleRowIndex}")
  private int exampleRowIndex;

  @Value("${reach.nipnots.spreadsheet.dataRowOffset}")
  private int dataRowOffset;

  @Value("${reach.nipnots.spreadsheet.startingRowIndex}")
  private int startingRowIndex;

  @Bean
  public NipnotSpreadsheetParserFactory nipnotSpreadsheetParserFactory() {
    return new NipnotSpreadsheetParserFactory(startingRowIndex, headerRowIndex, exampleRowIndex, dataRowOffset);
  }

}
