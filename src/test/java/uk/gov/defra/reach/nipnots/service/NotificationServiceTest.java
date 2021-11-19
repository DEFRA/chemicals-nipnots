package uk.gov.defra.reach.nipnots.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.defra.reach.nipnots.client.LegalEntityDetails;
import uk.gov.defra.reach.nipnots.client.MonitoringClient;
import uk.gov.defra.reach.nipnots.client.ReachClient;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;
import uk.gov.defra.reach.nipnots.dto.NipNotResponse;
import uk.gov.defra.reach.nipnots.dto.ValidationResult;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.LegalEntity;
import uk.gov.defra.reach.security.Role;
import uk.gov.defra.reach.security.User;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private static final InputStream INPUT_STREAM = new ByteArrayInputStream(new byte[]{1, 2, 3});
  private static final UUID LEGAL_ENTITY_ACCOUNT_ID = UUID.randomUUID();

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private NipnotSpreadsheetParserFactory parserFactory;

  @Mock
  private SpreadsheetParser spreadsheetParser;

  @Mock
  private SpreadsheetReader spreadsheetReader;

  @Mock
  private SpreadsheetValidator spreadsheetValidator;

  @Mock
  private NotificationManager notificationManager;

  @Mock
  private ReachClient reachClient;

  @Mock
  private NotifyService notifyService;

  @Mock
  private MonitoringClient monitoringClient;

  @BeforeEach
  public void setup() {
    notificationService = new NotificationService(spreadsheetReader, parserFactory, spreadsheetValidator, notificationManager, reachClient, notifyService, monitoringClient);
  }

  @Test
  void submitNotification_readsSpreadsheetAndWritesRows() {
    AuthenticatedUser testUser = mockSecurityPrincipal();

    Notification notification = createNotification();

    LegalEntityDetails legalEntityDetails = new LegalEntityDetails();

    List<String> rowValues1 = List.of("a", "b");
    List<String> rowValues2 = List.of("c", "d");
    Stream<List<String>> rowStream = Stream.of(rowValues1, rowValues2);
    NipNotRequest request = new NipNotRequest(UUID.randomUUID(), "filename");

    when(spreadsheetReader.retrieveSpreadsheet(request.getStorageLocation())).thenReturn(INPUT_STREAM);
    when(parserFactory.getInstance("filename", INPUT_STREAM)).thenReturn(spreadsheetParser);
    when(reachClient.getLegalEntityDetails(LEGAL_ENTITY_ACCOUNT_ID)).thenReturn(legalEntityDetails);
    when(spreadsheetParser.getRowStream()).thenReturn(rowStream);

    when(notificationManager.saveNewNotification(legalEntityDetails, "filename", rowStream)).thenReturn(notification);

    NipNotResponse nipNotResponse = notificationService.submitNotification(request);

    assertThat(nipNotResponse.getFileName()).isEqualTo(notification.getFileName());
    assertThat(nipNotResponse.getReferenceNumber()).isEqualTo(notification.getReferenceNumber());
    assertThat(nipNotResponse.getCreatedAt()).isEqualTo(notification.getCreatedAt());
    assertThat(nipNotResponse.getNumberOfSubstances()).isEqualTo(notification.getSubstances().size());

    verify(spreadsheetValidator).validateSpreadsheet(spreadsheetParser);
    verify(notifyService).sendNipnotConfirmationEmail(testUser, notification, true);
    verify(monitoringClient).sendNewNipNotsSpreadsheetEvent(notification);
  }

  @Test
  void validate_returnsValidationResult() {
    NipNotRequest request = new NipNotRequest(UUID.randomUUID(), "filename");

    when(spreadsheetReader.retrieveSpreadsheet(request.getStorageLocation())).thenReturn(INPUT_STREAM);
    when(parserFactory.getInstance("filename", INPUT_STREAM)).thenReturn(spreadsheetParser);
    when(spreadsheetValidator.validateSpreadsheet(spreadsheetParser)).thenReturn(ValidationResult.valid());

    ValidationResult result = notificationService.validate(request);

    assertThat(result).isEqualTo(ValidationResult.valid());
  }

  @Test
  void getLatestNotification_returnsNotification() {
    mockSecurityPrincipal();
    Notification notification = createNotification();
    when(notificationManager.getLatestNotificationForLegalEntity(LEGAL_ENTITY_ACCOUNT_ID)).thenReturn(Optional.of(notification));

    NipNotResponse nipNotResponse = notificationService.getLatestNotification();

    assertThat(nipNotResponse.getFileName()).isEqualTo(notification.getFileName());
    assertThat(nipNotResponse.getReferenceNumber()).isEqualTo(notification.getReferenceNumber());
    assertThat(nipNotResponse.getCreatedAt()).isEqualTo(notification.getCreatedAt());
    assertThat(nipNotResponse.getNumberOfSubstances()).isEqualTo(notification.getSubstances().size());
  }

  @Test
  void getLatestNotification_notFound() {
    mockSecurityPrincipal();
    when(notificationManager.getLatestNotificationForLegalEntity(LEGAL_ENTITY_ACCOUNT_ID)).thenReturn(Optional.empty());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> notificationService.getLatestNotification())
        .matches(ex -> ex.getStatus().equals(HttpStatus.NOT_FOUND));
  }

  private static AuthenticatedUser mockSecurityPrincipal() {
    Authentication authentication = Mockito.mock(Authentication.class);
    User user = new User();
    LegalEntity legalEntity = new LegalEntity();
    legalEntity.setAccountId(LEGAL_ENTITY_ACCOUNT_ID);
    AuthenticatedUser authenticatedUser = new AuthenticatedUser(user, legalEntity, Role.REACH_MANAGER);
    when(authentication.getPrincipal()).thenReturn(authenticatedUser);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return authenticatedUser;
  }

  private static Notification createNotification() {
    Notification notification = new Notification();
    notification.setReferenceNumber("ref1");
    notification.setCreatedAt(Instant.now());
    notification.setFileName("the file name.xlsx");
    notification.getSubstances().add(new NotificationSubstance());
    notification.getSubstances().add(new NotificationSubstance());
    notification.getSubstances().add(new NotificationSubstance());
    return notification;
  }

}

