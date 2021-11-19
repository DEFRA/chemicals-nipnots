package uk.gov.defra.reach.nipnots.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NipNotRequest {

  @NotNull
  private final UUID storageLocation;

  @NotEmpty
  private final String fileName;

  @JsonCreator
  public NipNotRequest(@JsonProperty UUID storageLocation, @JsonProperty String fileName) {
    this.storageLocation = storageLocation;
    this.fileName = fileName;
  }
}
