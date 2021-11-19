package uk.gov.defra.reach.nipnots.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.StringUtils;

/**
 * Excel specific implementation of {@link SpreadsheetParser}
 */
public class ExcelSpreadsheetParser implements SpreadsheetParser {

  private int headerColumnCount = 0;

  private int startingHashValue = 0;

  private int headerHashValue = 0;

  private int exampleRowHashValue = 0;

  private boolean isDataRowEmpty = true;

  private final int dataOffset;

  private final DataFormatter formatter = new DataFormatter();

  private final FormulaEvaluator formulaEvaluator;

  private final Workbook workbook;

  private final Sheet sheet;

  public ExcelSpreadsheetParser(InputStream inputStream, int sheetIndex, int startingRowIndex, int headerRowIndex, int exampleRowIndex, int dataOffset) {
    this.dataOffset = dataOffset;

    try {
      workbook = WorkbookFactory.create(inputStream);
      sheet = workbook.getSheetAt(sheetIndex);

      if (sheet.getRow(startingRowIndex) != null) {
        startingHashValue = getRowHash(startingRowIndex);
      }

      if (sheet.getRow(headerRowIndex) != null) {
        headerColumnCount = sheet.getRow(headerRowIndex).getLastCellNum();
        headerHashValue = getRowHash(headerRowIndex);
        exampleRowHashValue = getRowHash(exampleRowIndex);
      }

      if (sheet.getRow(dataOffset) != null) {
        isDataRowEmpty = readRowValuesAsList(sheet.getRow(dataOffset)).stream().allMatch(s -> s.equals(""));
      }

      formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
    } catch (UnsupportedFileFormatException | IOException e) {
      throw new SpreadsheetParserException("Error reading XSLX file", e);
    }
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

  private int getRowHash(int rowIndex) {
    StringBuilder rowString = new StringBuilder();

    if (sheet.getRow(rowIndex) == null) {
      return 0;
    }

    Iterator<Cell> cellIterator = sheet.getRow(rowIndex).cellIterator();

    while (cellIterator.hasNext()) {
      Cell cell = cellIterator.next();
      rowString.append(formatter.formatCellValue(cell, formulaEvaluator).trim().replaceAll("\n", ""));
    }
    return rowString.toString().hashCode();
  }

  @Override
  public int getHeaderColumnCount() {
    return headerColumnCount;
  }

  @Override
  public int getMaxRowColumnCount() {
    int maxColumnCount = 0;
    Iterator<Row> dataRows = getRowIteratorWithOffset();
    while (dataRows.hasNext()) {
      maxColumnCount = Math.max(maxColumnCount, dataRows.next().getLastCellNum());
    }
    return maxColumnCount;
  }

  @Override
  public Stream<List<String>> getRowStream() {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(getRowIteratorWithOffset(), 0), false)
        .map(this::readRowValuesAsList)
        .filter(row -> row.stream().anyMatch(StringUtils::hasLength));
  }

  @Override
  public int getStartingRowHash() {
    return startingHashValue;
  }

  @Override
  public void close() throws IOException {
    workbook.close();
  }

  private List<String> readRowValuesAsList(Row row) {
    List<String> cellValues = new ArrayList<>();
    for (int i = 0; i < headerColumnCount; i++) {
      Cell cell = row.getCell(i);
      cellValues.add(cell != null ? formatter.formatCellValue(cell, formulaEvaluator).trim() : "");
    }
    return cellValues;
  }

  private Iterator<Row> getRowIteratorWithOffset() {
    Iterator<Row> sheetRowIterator = sheet.iterator();
    for (int i = 0; i < dataOffset; i++) {
      sheetRowIterator.next();
    }
    return sheetRowIterator;
  }
}
