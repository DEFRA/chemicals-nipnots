package uk.gov.defra.reach.nipnots.parser;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

class OpenOfficeSpreadsheetParserTest {

  @Test
  void shouldReadData() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.ods");

    OpenOfficeSpreadsheetParser spreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0,1);

    assertThat(spreadsheetParser.getRowStream()).containsExactly(
        List.of("a", "b", "c"),
        List.of("aa", "bb", "cc"),
        List.of("aaa", "bbb", "ccc"));
  }

  @Test
  void shouldReadDataWithRowOffSet() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.ods");

    OpenOfficeSpreadsheetParser spreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 2);

    assertThat(spreadsheetParser.getRowStream()).containsExactly(
        List.of("aa", "bb", "cc"),
        List.of("aaa", "bbb", "ccc"));
  }

  @Test
  void shouldParseComplexSpreadsheet() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/complex.ods");

    OpenOfficeSpreadsheetParser spreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

    assertThat(spreadsheetParser.getRowStream()).containsExactly(
        // Row with empty cells in middle
        asList("a", null, "c", null, "e"),
        // Row with empty cells at beginning
        asList(null, null, null, null, "ee"),
        // Rows with empty cells at end
        asList("aa", "bb", "cc", null, null),
        asList("aaa", "bbb", "ccc", null, null),
        // Row with numbers
        asList("1", "0.1", "0.0000001", "1.34534534", "0"),
        // Row with booleans
        asList("FALSE", "TRUE", null, null, null),
        // Row with formulas
        asList("1", "101", "201", "301", "401"));
  }

  @Test
  void shouldCountHeaderColumns() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.ods");
    OpenOfficeSpreadsheetParser spreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

    assertThat(spreadsheetParser.getHeaderColumnCount()).isEqualTo(3);
  }

  @Test
  void correctTemplate_shouldMatchWithExpectedHashValues() {
    int expectedHeaderHash = -61294242;
    int expectedExampleRowHash = -896980211;
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/nipnots.ods");
    OpenOfficeSpreadsheetParser excelSpreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

    assertThat(excelSpreadsheetParser.getHeaderHash()).isEqualTo(expectedHeaderHash);
    assertThat(excelSpreadsheetParser.getExampleRowHash()).isEqualTo(expectedExampleRowHash);
  }

  @Test
  void incorrectTemplate_shouldNotMatchWithExpectedHashValues() {
    int expectedHeaderHash = -61294242;
    int expectedExampleRowHash = -896980211;
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/tampered_nipnots.ods");
    OpenOfficeSpreadsheetParser excelSpreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

    assertThat(excelSpreadsheetParser.getHeaderHash()).isNotEqualTo(expectedHeaderHash);
    assertThat(excelSpreadsheetParser.getExampleRowHash()).isNotEqualTo(expectedExampleRowHash);
  }

  @Test
  void correctTemplate_shouldContainDataRows() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/nipnots.ods");
    OpenOfficeSpreadsheetParser excelSpreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

    assertThat(excelSpreadsheetParser.isDataRowEmpty()).isFalse();
  }

  @Test
  void incorrectTemplate_shouldNotContainDataRows() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/tampered_nipnots.ods");
    OpenOfficeSpreadsheetParser excelSpreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

    assertThat(excelSpreadsheetParser.isDataRowEmpty()).isTrue();
  }

  @Test
  void shouldReturnMaximumRowColumnCount() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/complex.ods");
    OpenOfficeSpreadsheetParser spreadsheetParser = new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

    assertThat(spreadsheetParser.getHeaderColumnCount()).isEqualTo(5);
    assertThat(spreadsheetParser.getMaxRowColumnCount()).isEqualTo(6);
  }

  @Test
  void shouldThrowErrorForUnreadableFile() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.xlsx");
    assertThatThrownBy(() -> new OpenOfficeSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1)).isInstanceOf(SpreadsheetParserException.class);
  }

  @SneakyThrows
  private FileInputStream getInputStreamForFile(String fileName) {
    File spreadsheetFile = ResourceUtils.getFile(fileName);
    return new FileInputStream(spreadsheetFile);
  }

}
