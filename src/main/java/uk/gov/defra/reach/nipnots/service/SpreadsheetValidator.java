package uk.gov.defra.reach.nipnots.service;

import java.util.Collection;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.defra.reach.nipnots.dto.ValidationResult;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;

@Component
public class SpreadsheetValidator {

  @Value("${reach.nipnots.spreadsheet.expectedColumns}")
  private int expectedSpreadsheetColumnCount;

  @Value("${reach.nipnots.spreadsheet.maxCharacterCount}")
  private int maxCharacterCount;

  @Value("${reach.nipnots.spreadsheet.expectedStartingRowHash}")
  private int expectedStartingRowHashValue;

  @Value("${reach.nipnots.spreadsheet.expectedHeaderHash}")
  private int expectedHeaderHashValue;

  @Value("${reach.nipnots.spreadsheet.expectedExampleRowHash}")
  private int expectedExampleRowHashValue;

  public ValidationResult validateSpreadsheet(SpreadsheetParser spreadsheetParser) {
    int headerColumnCount = spreadsheetParser.getHeaderColumnCount();
    int startingRowHash = spreadsheetParser.getStartingRowHash();
    int headerColumnHash = spreadsheetParser.getHeaderHash();

    if (startingRowHash != expectedStartingRowHashValue) {
      return ValidationResult.invalid(ValidationErrorCode.INCORRECT_TEMPLATE_COLUMN_VALUES,
        "Spreadsheet does not have the correct header column names");
    }

    if (headerColumnCount != expectedSpreadsheetColumnCount) {
      return ValidationResult.invalid(ValidationErrorCode.WRONG_NUMBER_OF_COLUMNS,
          String.format("Spreadsheet should contain %d columns, but has %d", expectedSpreadsheetColumnCount,
              headerColumnCount));
    }

    if (headerColumnHash != expectedHeaderHashValue) {
      return ValidationResult.invalid(ValidationErrorCode.INCORRECT_TEMPLATE_COLUMN_VALUES, "Spreadsheet does not have the correct header column names");
    }
    
    int exampleRowHash = spreadsheetParser.getExampleRowHash();

    if (exampleRowHash == 0) {
      return ValidationResult.invalid(ValidationErrorCode.MISSING_EXAMPLE_ROW, "Missing example row from the spreadsheet");
    }

    if (exampleRowHash != expectedExampleRowHashValue) {
      return ValidationResult.invalid(ValidationErrorCode.INCORRECT_TEMPLATE_COLUMN_VALUES, "Spreadsheet does not have the correct example row values");
    }

    if (headerColumnCount < spreadsheetParser.getMaxRowColumnCount()) {
      return ValidationResult.invalid(ValidationErrorCode.WRONG_NUMBER_OF_COLUMNS,
          "One or more rows contain more values than the number of headers");
    }

    if (spreadsheetParser.isDataRowEmpty()) {
      return ValidationResult.invalid(ValidationErrorCode.MISSING_DATA_ROW,"Spreadsheet does not have the first data row populated with values");
    }

    long totalSize = spreadsheetParser.getRowStream()
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .map(String::length)
        .mapToLong(i -> i)
        .sum();

    if (totalSize > maxCharacterCount) {
      return ValidationResult.invalid(ValidationErrorCode.FILE_SIZE_LIMIT_EXCEEDED,
          String.format("Total number of characters is %d which exceed the maximum %d allowed", totalSize, maxCharacterCount));
    }
    return ValidationResult.valid();
  }
}
