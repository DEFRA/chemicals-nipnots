package uk.gov.defra.reach.nipnots.parser;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

class ExcelSpreadsheetParserTest {

  @Test
  @SneakyThrows
  void shouldReadDataFromXlsx() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.xlsx");

    ExcelSpreadsheetParser excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

    assertThat(excelSpreadsheetParser.getRowStream()).containsExactly(
        List.of("a", "b", "c"),
        List.of("aa", "bb", "cc"),
        List.of("aaa", "bbb", "ccc"));

    excelSpreadsheetParser.close();
  }

  @Test
  void shouldReadDataWithRowOffSet() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.xlsx");

    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 2);
  
      assertThat(excelSpreadsheetParser.getRowStream()).containsExactly(
          List.of("aa", "bb", "cc"),
          List.of("aaa", "bbb", "ccc"));
    
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void shouldParseComplexSpreadsheet() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/complex.xlsx");

    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

      assertThat(excelSpreadsheetParser.getRowStream()).containsExactly(
          // Row with empty cells in middle
          asList("a", "", "c", "", "e"),
          // Row with empty cells at beginning
          asList("", "", "", "", "ee"),
          // Rows with empty cells at end
          asList("aa", "bb", "cc", "", ""),
          asList("aaa", "bbb", "ccc", "", ""),
          // Row with numbers
          asList("1", "0.1", "0.0000001", "1.34534534", "0"),
          // Row with booleans
          asList("FALSE", "TRUE", "", "", ""),
          // Row with formulas
          asList("1", "101", "201", "301", "401"));
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void shouldCountHeaderColumns() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

      assertThat(excelSpreadsheetParser.getHeaderColumnCount()).isEqualTo(3);
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void incorrectTemplate_shouldDefaultRowHashToZeroIfRowMissing() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

      assertThat(excelSpreadsheetParser.getExampleRowHash()).isEqualTo(0);
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void correctTemplate_shouldMatchWithExpectedHashValues() throws Exception {
    int expectedHeaderHash = -61294242;
    int expectedExampleRowHash = -896980211;
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/nipnots.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

      assertThat(excelSpreadsheetParser.getHeaderHash()).isEqualTo(expectedHeaderHash);
      assertThat(excelSpreadsheetParser.getExampleRowHash()).isEqualTo(expectedExampleRowHash);
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  private void closeParser(ExcelSpreadsheetParser excelSpreadsheetParser) throws IOException {
    if (excelSpreadsheetParser != null) {
      excelSpreadsheetParser.close();
    }
  }

  @Test
  void incorrectTemplate_shouldNotMatchWithExpectedHashValues() throws Exception {
    int expectedHeaderHash = -61294242;
    int expectedExampleRowHash = -896980211;
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/tampered_nipnots.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

      assertThat(excelSpreadsheetParser.getHeaderHash()).isNotEqualTo(expectedHeaderHash);
      assertThat(excelSpreadsheetParser.getExampleRowHash()).isNotEqualTo(expectedExampleRowHash);
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void correctTemplate_shouldContainDataRows() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/nipnots.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

      assertThat(excelSpreadsheetParser.isDataRowEmpty()).isFalse();
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void incorrectTemplate_shouldNotContainDataRows() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/tampered_nipnots.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
      excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 1, 3, 4, 5);

      assertThat(excelSpreadsheetParser.isDataRowEmpty()).isTrue();
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void shouldReturnMaximumRowColumnCount() throws Exception {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/complex.xlsx");
    ExcelSpreadsheetParser excelSpreadsheetParser = null;
    try {
     excelSpreadsheetParser = new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1);

    assertThat(excelSpreadsheetParser.getHeaderColumnCount()).isEqualTo(5);
    assertThat(excelSpreadsheetParser.getMaxRowColumnCount()).isEqualTo(6);
    } finally {
      closeParser(excelSpreadsheetParser);
    }
  }

  @Test
  void shouldThrowErrorForUnreadableFile() {
    FileInputStream fileInputStream = getInputStreamForFile("classpath:spreadsheets/simple.ods");
    assertThatThrownBy(() -> new ExcelSpreadsheetParser(fileInputStream, 0, 0, 0, 0, 1)).isInstanceOf(SpreadsheetParserException.class);
  }

  @SneakyThrows
  private FileInputStream getInputStreamForFile(String fileName) {
    File spreadsheetFile = ResourceUtils.getFile(fileName);
    return new FileInputStream(spreadsheetFile);
  }

}
