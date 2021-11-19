package uk.gov.defra.reach.nipnots.service;

import java.io.InputStream;
import uk.gov.defra.reach.nipnots.parser.ExcelSpreadsheetParser;
import uk.gov.defra.reach.nipnots.parser.OpenOfficeSpreadsheetParser;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;

/**
 * Factory for creating {@link SpreadsheetParser} instances
 */
public class NipnotSpreadsheetParserFactory {

  private static final int SHEET_0 = 0;

  private final int startingRow;

  private final int headerRow;

  private final int exampleRow;

  private final int dataOffset;

  public NipnotSpreadsheetParserFactory(int startingRow, int headerRow, int exampleRow, int dataOffset) {
    this.startingRow = startingRow;
    this.headerRow = headerRow;
    this.exampleRow = exampleRow;
    this.dataOffset = dataOffset;
  }

  /**
   * Returns a {@code SpreadsheetParser} of the correct type for reading the given {@code InputStream}
   *
   * @param fileName the file name of the spreadsheet file requiring parsing
   * @param inputStream input stream of the contents of the file
   * @return a new Spreadsheet parser
   * @throws IllegalArgumentException if the file name suffix is not supported
   */
  public SpreadsheetParser getInstance(String fileName, InputStream inputStream) {
    if (fileName.endsWith(SpreadsheetFileExtensions.EXCEL_EXTENSION)) {
      return new ExcelSpreadsheetParser(inputStream, SHEET_0, startingRow, headerRow, exampleRow, dataOffset);
    } else if (fileName.endsWith(SpreadsheetFileExtensions.OPEN_OFFICE_EXTENSION)) {
      return new OpenOfficeSpreadsheetParser(inputStream, SHEET_0, startingRow, headerRow, exampleRow, dataOffset);
    } else {
      throw new IllegalArgumentException("Unable to create appropriate parser for file with name " + fileName);
    }
  }

}
