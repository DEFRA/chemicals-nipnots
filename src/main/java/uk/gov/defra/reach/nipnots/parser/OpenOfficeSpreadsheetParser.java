package uk.gov.defra.reach.nipnots.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.springframework.util.StringUtils;

/**
 * Open Office specific implementation of {@link SpreadsheetParser}
 */
public class OpenOfficeSpreadsheetParser implements SpreadsheetParser {

  private int headerColumnCount = 0;

  private int startingHashValue = 0;

  private int headerHashValue = 0;

  private int exampleRowHashValue = 0;

  private boolean isDataRowEmpty = true;

  private final int dataOffset;

  private final OdfSpreadsheetDocument document;

  private final OdfTable table;

  private List<OdfTableRow> rows;

  public OpenOfficeSpreadsheetParser(InputStream inputStream, int tableIndex, int startingRowIndex, int headerRowIndex, int exampleRowIndex, int dataOffset) {
    this.dataOffset = dataOffset;

    try {
      document = OdfSpreadsheetDocument.loadDocument(inputStream);
      table = document.getTableList().get(tableIndex);

      if (table.getRowByIndex(headerRowIndex) != null) {
        headerColumnCount = calculateColumnCount(table.getRowByIndex(headerRowIndex));
        headerHashValue = getRowHash(headerRowIndex);
        exampleRowHashValue = getRowHash(exampleRowIndex);
      }

      if (table.getRowByIndex(startingRowIndex) != null) {
        startingHashValue = getRowHash(startingRowIndex);
      }

      if (table.getRowByIndex(dataOffset) != null) {
        isDataRowEmpty = readRowValuesAsList(table.getRowByIndex(dataOffset)).stream().allMatch(Objects::isNull);
      }

    } catch (Exception e) {
      throw new SpreadsheetParserException("Error reading ODS file", e);
    }
  }

  private int getRowHash(int rowIndex) {
    StringBuilder rowString = new StringBuilder();
    OdfTableRow row = table.getRowByIndex(rowIndex);

    for (int i = 0; i < headerColumnCount; i++) {
      OdfTableCell cell = row.getCellByIndex(i);
      rowString.append(StringUtils.hasText(cell.getDisplayText()) ? cell.getDisplayText().trim().replaceAll("\n", "") : "");
    }
    return rowString.toString().hashCode();
  }


  public int getHeaderColumnCount() {
    return headerColumnCount;
  }

  @Override
  public int getMaxRowColumnCount() {
    int maxColumnCount = 0;
    for (OdfTableRow row : getRows()) {
      if (row.getOdfElement().getRepetition() <= 1) {
        maxColumnCount = Math.max(maxColumnCount, calculateColumnCount(row));
      }
    }
    return maxColumnCount;
  }

  @Override
  public int getHeaderHash() {
    return headerHashValue;
  }

  @Override
  public int getExampleRowHash() {
    return exampleRowHashValue;
  }

  @Override
  public boolean isDataRowEmpty() {
    return isDataRowEmpty;
  }

  @Override
  public Stream<List<String>> getRowStream() {
    return getRows().subList(dataOffset, getRows().size()).stream()
        .filter(row -> row.getOdfElement().getRepetition() <= 1)
        .map(this::readRowValuesAsList)
        .filter(row -> row.stream().anyMatch(StringUtils::hasLength));
  }

  @Override
  public int getStartingRowHash() {
    return startingHashValue;
  }

  @Override
  public void close() {
    document.close();
  }

  private List<OdfTableRow> getRows() {
    if (rows == null) {
      rows = table.getRowList();
    }
    return rows;
  }

  private List<String> readRowValuesAsList(OdfTableRow row) {
    List<String> cellValues = new ArrayList<>();
    for (int i = 0; i < headerColumnCount; i++) {
      OdfTableCell cell = row.getCellByIndex(i);
      cellValues.add(StringUtils.hasText(cell.getDisplayText()) ? cell.getDisplayText() : null);
    }
    return cellValues;
  }

  private static int calculateColumnCount(OdfTableRow row) {
    int count = 0;
    while (true) {
      OdfTableCell cell = row.getCellByIndex(count);
      if (cell == null || !StringUtils.hasLength(cell.getDisplayText())) {
        break;
      }
      count++;
    }
    return count;
  }
}
