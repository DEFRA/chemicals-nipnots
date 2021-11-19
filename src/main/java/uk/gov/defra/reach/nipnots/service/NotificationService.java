package uk.gov.defra.reach.nipnots.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.defra.reach.nipnots.client.LegalEntityDetails;
import uk.gov.defra.reach.nipnots.client.MonitoringClient;
import uk.gov.defra.reach.nipnots.client.ReachClient;
import uk.gov.defra.reach.nipnots.dto.NipNotRequest;
import uk.gov.defra.reach.nipnots.dto.NipNotResponse;
import uk.gov.defra.reach.nipnots.dto.ValidationResult;
import uk.gov.defra.reach.nipnots.entity.Notification;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParser;
import uk.gov.defra.reach.nipnots.parser.SpreadsheetParserException;
import uk.gov.defra.reach.security.AuthenticatedUser;


@Service
@Slf4j
public class NotificationService {

  private final SpreadsheetReader spreadsheetReader;

  private final NipnotSpreadsheetParserFactory parserFactory;

  private final SpreadsheetValidator spreadsheetValidator;

  private final NotificationManager notificationManager;

  private final ReachClient reachClient;

  private final NotifyService notifyService;

  private final MonitoringClient monitoringClient;

  @Autowired
  public NotificationService(SpreadsheetReader spreadsheetReader,
                             NipnotSpreadsheetParserFactory parserFactory, SpreadsheetValidator spreadsheetValidator,
                             NotificationManager notificationManager, ReachClient reachClient, NotifyService notifyService, MonitoringClient monitoringClient) {
    this.spreadsheetReader = spreadsheetReader;
    this.parserFactory = parserFactory;
    this.spreadsheetValidator = spreadsheetValidator;
    this.notificationManager = notificationManager;
    this.reachClient = reachClient;
    this.notifyService = notifyService;
    this.monitoringClient = monitoringClient;
  }

  /**
   * Creates a new NipNot Notification based upon the contents of a spreadsheet uploaded by the user
   *
   * @param request details of the spreadsheet
   * @return a response for the submission
   */
  public NipNotResponse submitNotification(NipNotRequest request) {
    try (InputStream inputStream = spreadsheetReader.retrieveSpreadsheet(request.getStorageLocation())) {
      SpreadsheetParser spreadsheetParser = parserFactory.getInstance(request.getFileName(), inputStream);
      spreadsheetValidator.validateSpreadsheet(spreadsheetParser);

      AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      LegalEntityDetails legalEntity = reachClient.getLegalEntityDetails(authenticatedUser.getLegalEntity().getAccountId());
      boolean isNewSubmission = notificationManager.getLatestNotificationForLegalEntity(legalEntity.getAccountId()).isEmpty();
      Notification notification = notificationManager.saveNewNotification(legalEntity, request.getFileName(), spreadsheetParser.getRowStream());

      log.info("Successfully processed spreadsheet containing {} substances to for legal entity {}", notification.getSubstances().size(),
          notification.getLegalEntityAccountId());

      notifyService.sendNipnotConfirmationEmail(authenticatedUser, notification, isNewSubmission);
      monitoringClient.sendNewNipNotsSpreadsheetEvent(notification);
      return mapToResponse(notification);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Creates a new Nipnot validation result based on the contents of the spreadsheet uploaded by the user
   *
   * @param request details of the spreadsheet
   * @return a JSON body response containing the validity of the spreadsheet, error code and description
   */
  public ValidationResult validate(NipNotRequest request) {
    SpreadsheetParser spreadsheetParser;

    try (InputStream inputStream = spreadsheetReader.retrieveSpreadsheet(request.getStorageLocation())) {
      PreParseValidator.validate(spreadsheetReader, request);
      spreadsheetParser = parserFactory.getInstance(request.getFileName(), inputStream);
    } catch (InvalidFormatException e) {
      log.error("Error parsing spreadsheet as it was strict ooxml", e);
      return ValidationResult.invalid(ValidationErrorCode.WRONG_FORMAT, e.getMessage());
    } catch (SpreadsheetParserException | IOException e) {
      log.error("Error parsing spreadsheet for validation", e);
      return ValidationResult.invalid(ValidationErrorCode.UNREADABLE_FILE, e.getMessage());
    }
    return spreadsheetValidator.validateSpreadsheet(spreadsheetParser);
  }

  /**
   * Returns details of the latest notification for the current legal entity
   *
   * @return details of the notification
   * @throws ResponseStatusException 404 if there is no notification
   */
  @Transactional
  public NipNotResponse getLatestNotification() {
    UUID legalEntityId = ((AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLegalEntity().getAccountId();
    Optional<Notification> notification = notificationManager.getLatestNotificationForLegalEntity(legalEntityId);
    return notification.map(NotificationService::mapToResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private static NipNotResponse mapToResponse(Notification notification) {
    return NipNotResponse.builder()
        .createdAt(notification.getCreatedAt())
        .fileName(notification.getFileName())
        .referenceNumber(notification.getReferenceNumber())
        .numberOfSubstances(notification.getSubstances().size())
        .build();
  }
}
