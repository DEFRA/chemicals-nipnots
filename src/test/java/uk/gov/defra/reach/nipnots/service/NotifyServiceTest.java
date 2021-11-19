package uk.gov.defra.reach.nipnots.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.defra.reach.nipnots.client.AuditClient;
import uk.gov.defra.reach.nipnots.client.NotifyClient;
import uk.gov.defra.reach.nipnots.dto.EmailNotification;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.entity.NotificationSubstance;
import uk.gov.defra.reach.nipnots.mapper.NipNotsConfirmationBuilder;
import uk.gov.defra.reach.security.AuthenticatedUser;
import uk.gov.defra.reach.security.LegalEntity;
import uk.gov.defra.reach.security.Role;
import uk.gov.defra.reach.security.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotifyServiceTest {
  private static final UUID LEGAL_ENTITY_ACCOUNT_ID = UUID.randomUUID();

  @InjectMocks
  private NotifyService notifyService;
  @Mock
  private NipNotsConfirmationBuilder nipNotsConfirmationBuilder;
  @Mock
  private NotifyClient notifyClient;
  @Mock
  private AuditClient auditClient;

  private Logger logger;

  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  public void setup() {
    logger = (Logger) LoggerFactory.getLogger(NotifyService.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    notifyService = new NotifyService(nipNotsConfirmationBuilder, notifyClient, auditClient);
  }

  @Test
  public void sendNewSubmissionConfirmationEmail_shouldCallReachNotify() {
    AuthenticatedUser user = createTestAccount();
    String action = "submitted";
    Notification testResponse = new Notification();
    testResponse.setSubstances(Set.of(new NotificationSubstance()));
    testResponse.setFileName("test-file.xlsx");

    EmailNotification testNotification = new EmailNotification("test event", "test@email.com", Map.of("test event", "test"));

    when(nipNotsConfirmationBuilder.create(user.getUser().getEmailAddress().orElse("test"), testResponse, action)).thenReturn(testNotification);
    when(notifyClient.sendNotification(testNotification)).thenReturn(HttpStatus.CREATED);
    notifyService.sendNipnotConfirmationEmail(user, testResponse, true);

    verify(notifyClient).sendNotification(testNotification);
  }

  @Test
  public void sendUpdatedSubmissionConfirmationEmail_shouldCallReachNotify() {
    AuthenticatedUser user = createTestAccount();
    String action = "updated";
    Notification testResponse = new Notification();
    testResponse.setSubstances(Set.of(new NotificationSubstance()));
    testResponse.setFileName("test-file.xlsx");

    EmailNotification testNotification = new EmailNotification("test event", "test@email.com", Map.of("test event", "test"));

    when(nipNotsConfirmationBuilder.create(user.getUser().getEmailAddress().orElse("test"), testResponse, action)).thenReturn(testNotification);
    when(notifyClient.sendNotification(testNotification)).thenReturn(HttpStatus.CREATED);
    notifyService.sendNipnotConfirmationEmail(user, testResponse, false);

    verify(notifyClient).sendNotification(testNotification);
  }

  @Test
  public void sendNipnotConfirmationEmail_shouldLogIfNotifyFails() {
    AuthenticatedUser user = createTestAccount();

    Notification testResponse = new Notification();
    testResponse.setSubstances(Set.of(new NotificationSubstance()));
    testResponse.setFileName("test-file.xlsx");;
    String action = "submitted";

    EmailNotification testNotification = new EmailNotification("test event", "test@email.com", Map.of("test event", "test"));

    when(nipNotsConfirmationBuilder.create(user.getUser().getEmailAddress().orElse("test"), testResponse, action)).thenReturn(testNotification);
    when(notifyClient.sendNotification(testNotification)).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    notifyService.sendNipnotConfirmationEmail(user, testResponse, true);

    List<ILoggingEvent> logList = listAppender.list;
    assertThat(logList.get(0).getFormattedMessage()).isEqualTo("Could not send email notification to test@email.com");
  }

  private static AuthenticatedUser createTestAccount() {
    String email = "test@email.com";
    User user = new User();
    user.setEmailAddress(Optional.of(email));
    user.setContactId(Optional.of(UUID.randomUUID()));

    LegalEntity legalEntity = new LegalEntity();
    legalEntity.setAccountId(LEGAL_ENTITY_ACCOUNT_ID);
    return new AuthenticatedUser(user, legalEntity, Role.REACH_MANAGER);
  }
}
