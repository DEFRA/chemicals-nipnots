package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import lombok.SneakyThrows;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;

@ExtendWith(MockitoExtension.class)
public class PreParseValidatorTest {

  @Mock 
  private SpreadsheetReader spreadsheetReader;
  
  @Mock
  private NipNotRequest request;
  
  
  private UUID STORAGE_LOCATION = UUID.randomUUID();
  
  @Test
  public void should_throw_exception_with_ooxml() {

    when(request.getFileName()).thenReturn("TestFile." + SpreadsheetFileExtensions.EXCEL_EXTENSION);
    when(request.getStorageLocation()).thenReturn(STORAGE_LOCATION);
    when(spreadsheetReader.retrieveSpreadsheet(STORAGE_LOCATION)).thenReturn(getInputStreamForFile());
    
    assertThatExceptionOfType(InvalidFormatException.class).isThrownBy(() -> PreParseValidator.validate(spreadsheetReader, request));
    
  }
  
  @Test
  public void should_do_nothing_for_ods() throws Exception{

    when(request.getFileName()).thenReturn("TestFile." + SpreadsheetFileExtensions.OPEN_OFFICE_EXTENSION);

    assertDoesNotThrow(() -> PreParseValidator.validate(spreadsheetReader, request));
  }
  
  @SneakyThrows
  private static FileInputStream getInputStreamForFile() {
    File spreadsheetFile = ResourceUtils.getFile("classpath:spreadsheets/strict_xml.xlsx");
    return new FileInputStream(spreadsheetFile);
  }
}
