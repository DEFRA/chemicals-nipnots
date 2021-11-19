package uk.gov.defra.reach.nipnots.parser;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Stream;

/**
 * A parser capable of extracting data from a spreadsheet file.
 */
public interface SpreadsheetParser extends Closeable {

  /**
   * Returns the number of consecutive non-empty cells in the header row starting from column A.
   *
   * @return the number of header columns
   */
  int getHeaderColumnCount();

  /**
   * Returns the index of the last non-empty cell in the longest row
   *
   * @return the max column index
   */
  int getMaxRowColumnCount();

  /**
   * Returns a stream containing data for every non-empty row
   *
   * @return a Stream of row data
   */
  Stream<List<String>> getRowStream();

  /**
   * Returns the hash value equivalent of the first row
   *
   * @return the hash equivalent of the first row
   */
  int getStartingRowHash();

  /**
   * Returns the hash value equivalent of header row column names
   *
   * @return the hash equivalent of header row values
   */
  int getHeaderHash();

  /**
   * Returns the hash value equivalent of example row column values
   *
   * @return the hash equivalent of example row values
   */
  int getExampleRowHash();

  /**
   * Returns a boolean flag to check if the first data row is populated
   *
   * @return the boolean flag
   */
  boolean isDataRowEmpty();
}
