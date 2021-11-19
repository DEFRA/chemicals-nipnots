package uk.gov.defra.reach.nipnots.service;

public class SpreadsheetValidationException extends RuntimeException {

  private static final long serialVersionUID = 8397867091571326468L;

  public SpreadsheetValidationException(String message) {
    super(message);
  }
}
