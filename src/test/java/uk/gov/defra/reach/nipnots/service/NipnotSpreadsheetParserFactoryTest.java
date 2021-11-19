package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.FileInputStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;

class NipnotSpreadsheetParserFactoryTest {

  @Test
  void shouldCreateExcelParser() {
    NipnotSpreadsheetParserFactory parserFactory = new NipnotSpreadsheetParserFactory(0, 0, 0, 1);
    FileInputStream fileInputStream = getInputStreamForExcelFile();

    SpreadsheetParser parser = parserFactory.getInstance("file.xlsx", fileInputStream);
    assertThat(parser.getHeaderColumnCount()).isEqualTo(3);
  }

  @Test
  void shouldCreateOpenOfficeParser() {
    NipnotSpreadsheetParserFactory parserFactory = new NipnotSpreadsheetParserFactory(0, 0, 0, 1);
    FileInputStream fileInputStream = getInputStreamForOpenOfficeFile();

    SpreadsheetParser parser = parserFactory.getInstance("file.ods", fileInputStream);
    assertThat(parser.getHeaderColumnCount()).isEqualTo(3);
  }

  @Test
  void shouldThrowExceptionForUnhandledFileType() {
    NipnotSpreadsheetParserFactory parserFactory = new NipnotSpreadsheetParserFactory(0, 0, 0, 1);
    FileInputStream fileInputStream = getInputStreamForOpenOfficeFile();

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parserFactory.getInstance("file.xxx", fileInputStream));

  }

  @SneakyThrows
  private static FileInputStream getInputStreamForExcelFile() {
    File spreadsheetFile = ResourceUtils.getFile("classpath:spreadsheets/simple.xlsx");
    return new FileInputStream(spreadsheetFile);
  }

  @SneakyThrows
  private FileInputStream getInputStreamForOpenOfficeFile() {
    File spreadsheetFile = ResourceUtils.getFile("classpath:spreadsheets/simple.ods");
    return new FileInputStream(spreadsheetFile);
  }

}
