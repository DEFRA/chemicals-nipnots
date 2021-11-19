package uk.gov.defra.reach.nipnots.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-dev.properties")
public class NipNotsConfirmationBuilderTest {
  private static final String URL = "test-url.com";
  private static final String FILENAME = "test-file.xlsx";
  private static final String SUBMITTED_DATE = "18 January 2021";

  private NipNotsConfirmationBuilder nipNotsConfirmationBuilder = new NipNotsConfirmationBuilder();

  @BeforeEach
  void setup() {
    nipNotsConfirmationBuilder = new NipNotsConfirmationBuilder();
    ReflectionTestUtils.setField(nipNotsConfirmationBuilder, "chemicalRegulationUrl", URL);
  }

  @Test
  public void shouldCreateEmailTemplateData() throws ParseException {
    Date submittedDate = new SimpleDateFormat("dd/MM/yyyy").parse("18/01/2021");
    Notification data = new Notification();
    String action = "submitted";

    data.setSubstances(Set.of(new NotificationSubstance()));
    data.setFileName(FILENAME);
    data.setCreatedAt(submittedDate.toInstant());
    data.setReferenceNumber("123456");

    EmailNotification actualData = nipNotsConfirmationBuilder.create("test@email.com", data, action);

    assertThat(actualData.getPersonalisation().get("filename")).isEqualTo(FILENAME);
    assertThat(actualData.getPersonalisation().get("submission_date")).isEqualTo(SUBMITTED_DATE);
    assertThat(actualData.getPersonalisation().get("number_of_substances")).isEqualTo("1");
    assertThat(actualData.getPersonalisation().get("reach_url")).isEqualTo(URL);
    assertThat(actualData.getPersonalisation().get("action")).isEqualTo(action);
  }
}
