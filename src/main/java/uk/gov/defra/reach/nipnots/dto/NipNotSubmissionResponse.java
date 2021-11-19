package uk.gov.defra.reach.nipnots.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
public class NipNotSubmissionResponse {

  private final UUID submissionId;

  @JsonCreator
  public NipNotSubmissionResponse(@JsonProperty("submissionId") UUID submissionId) {
    this.submissionId = submissionId;
  }

}
