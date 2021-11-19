package uk.gov.defra.reach.nipnots.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

import uk.gov.defra.reach.nipnots.dto.NipNotRequest;

public class PreParseValidator {
  
  private PreParseValidator() {
  }

  public static void validate(SpreadsheetReader spreadsheetReader, NipNotRequest request)
      throws IOException, InvalidFormatException {
    if (request.getFileName().endsWith(SpreadsheetFileExtensions.EXCEL_EXTENSION)) {
      try (InputStream inputStream = spreadsheetReader.retrieveSpreadsheet(request.getStorageLocation())) {

        OPCPackage pkg = OPCPackage.open(inputStream);
        PackageRelationshipCollection coreDocRelationships = pkg
            .getRelationshipsByType(PackageRelationshipTypes.STRICT_CORE_DOCUMENT);

        if (coreDocRelationships.size() > 0) {
          throw new InvalidFormatException("The uploaded spreadsheet - Strict Open XML Spreadsheet is not supported");
        }
      }
    }
  }
}
