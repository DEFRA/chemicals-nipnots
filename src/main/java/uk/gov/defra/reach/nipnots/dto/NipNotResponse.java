package uk.gov.defra.reach.nipnots.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NipNotResponse {

  private String fileName;

  private Instant createdAt;

  private String referenceNumber;

  private int numberOfSubstances;

}
