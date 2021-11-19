package uk.gov.defra.reach.nipnots.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.defra.reach.nipnots.service.ValidationErrorCode;

@Data
public class ValidationResult {

  @JsonProperty("valid")
  private final boolean valid;
  @JsonProperty("errorCode")
  private final ValidationErrorCode errorCode;
  @JsonProperty("details")
  private final String details;

  public static ValidationResult valid() {
    return new ValidationResult(true, null, null);
  }

  public static ValidationResult invalid(ValidationErrorCode errorCode, String details) {
    return new ValidationResult(false, errorCode, details);
  }

}
