package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.defra.reach.nipnots.dto.ValidationResult;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;

@ExtendWith(MockitoExtension.class)
class SpreadsheetValidatorTest {

  private static final int COLUMN_COUNT = 3;
  private static final int MAX_CHAR_COUNT = 10;
  private static final int EXPECTED_EXAMPLE_ROW_HASH = -896980211;
  private static final int EXPECTED_HEADER_ROW_HASH = -61294242;

  private SpreadsheetValidator spreadsheetValidator;

  @Mock
  private SpreadsheetParser spreadsheetParser;

  @BeforeEach
  void setup() {
    spreadsheetValidator = new SpreadsheetValidator();
    ReflectionTestUtils.setField(spreadsheetValidator, "expectedSpreadsheetColumnCount", COLUMN_COUNT);
    ReflectionTestUtils.setField(spreadsheetValidator, "maxCharacterCount", MAX_CHAR_COUNT);
    ReflectionTestUtils.setField(spreadsheetValidator, "expectedHeaderHashValue", EXPECTED_HEADER_ROW_HASH);
    ReflectionTestUtils.setField(spreadsheetValidator, "expectedExampleRowHashValue", EXPECTED_EXAMPLE_ROW_HASH);
  }

  @Test
  void validateSpreadsheet_acceptsValidSpreadsheet() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(EXPECTED_EXAMPLE_ROW_HASH);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);
    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.valid());
  }

  @Test
  void validateSpreadsheet_throwsExceptionForWrongNumberOfColumns() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(2);
    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.WRONG_NUMBER_OF_COLUMNS,
        String.format("Spreadsheet should contain %d columns, but has %d", COLUMN_COUNT,
            2)));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForTooManyColumnsInDataRow() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(EXPECTED_EXAMPLE_ROW_HASH);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);
    when(spreadsheetParser.getMaxRowColumnCount()).thenReturn(4);

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.WRONG_NUMBER_OF_COLUMNS,
        String.format("One or more rows contain more values than the number of headers", COLUMN_COUNT, 4)));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForHeaderRowHashMismatch() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH - 1);

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.INCORRECT_TEMPLATE_COLUMN_VALUES, "Spreadsheet does not have the correct header column names"));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForMissingExampleRow() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(0);

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.MISSING_EXAMPLE_ROW, "Missing example row from the spreadsheet"));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForExampleRowHashMismatch() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(EXPECTED_EXAMPLE_ROW_HASH - 1);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.INCORRECT_TEMPLATE_COLUMN_VALUES, "Spreadsheet does not have the correct example row values"));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForMissingDataRow() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(EXPECTED_EXAMPLE_ROW_HASH);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);
    when(spreadsheetParser.isDataRowEmpty()).thenReturn(true);

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(ValidationErrorCode.MISSING_DATA_ROW, "Spreadsheet does not have the first data row populated with values"));
  }

  @Test
  void validateSpreadsheet_throwsExceptionForTooManyCharacters() {
    when(spreadsheetParser.getHeaderColumnCount()).thenReturn(COLUMN_COUNT);
    when(spreadsheetParser.getExampleRowHash()).thenReturn(EXPECTED_EXAMPLE_ROW_HASH);
    when(spreadsheetParser.getHeaderHash()).thenReturn(EXPECTED_HEADER_ROW_HASH);
    when(spreadsheetParser.getRowStream()).thenReturn(Stream.of(
        List.of("1", "2", "3"),
        List.of("4", "5", "6"),
        List.of("7", "8", "9"),
        List.of("10")));

    assertThat(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).isEqualTo(ValidationResult.invalid(
        ValidationErrorCode.FILE_SIZE_LIMIT_EXCEEDED, String.format("Total number of characters is %d which exceed the maximum %d allowed",
            11, MAX_CHAR_COUNT)));
  }

}
