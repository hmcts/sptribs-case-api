package uk.gov.hmcts.sptribs.caseworker.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_TO_DATABASE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_WITH_NO_FILENAME_TO_DATABASE;

@Slf4j
public final class MessageUtil {

    private static final String REPRESENTATIVE = "Representative";
    private static final String RESPONDENT = "Respondent";
    private static final String SUBJECT = "Subject";
    private static final String APPLICANT = "Applicant";
    private static final String TRIBUNAL = "Tribunal";

    private static final String COMMA_SPACE = ", ";

    private MessageUtil() {
    }

    public static String generateSimpleMessage(String header, String footer) {
        String message = format("# %s", header);
        if (StringUtils.hasText(footer)) {
            message = message + format(" %n## %s", footer);
        }
        return message;
    }

    public static String generateSimpleMessage(final CicCase cicCase, String header, String footer) {
        final String notificationMessage = generateSimpleMessage(cicCase);
        String message = format("# %s %n## %s", header, notificationMessage);
        if (StringUtils.hasText(footer)) {
            message = message + format(" %n## %s", footer);
        }
        return message;
    }

    public static String generateSimpleMessage(final CicCase cicCase) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            message.append(SUBJECT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            message.append(RESPONDENT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            message.append(REPRESENTATIVE + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            message.append(APPLICANT + COMMA_SPACE);
        }
        return message.substring(0, message.length() - 2);
    }

    public static String generateSimpleMessage(Set<NotificationParties> notificationParties) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");
        if (notificationParties.contains(NotificationParties.SUBJECT)) {
            message.append(SUBJECT + COMMA_SPACE);
        }
        if (notificationParties.contains(NotificationParties.RESPONDENT)) {
            message.append(RESPONDENT + COMMA_SPACE);
        }
        if (notificationParties.contains(NotificationParties.REPRESENTATIVE)) {
            message.append(REPRESENTATIVE + COMMA_SPACE);
        }
        if (notificationParties.contains(NotificationParties.APPLICANT)) {
            message.append(APPLICANT + COMMA_SPACE);
        }
        return message.substring(0, message.length() - 2);
    }

    public static String generateSimpleMessage(final ContactParties contactParties) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");

        if (!CollectionUtils.isEmpty(contactParties.getSubjectContactParties())) {
            message.append(SUBJECT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(contactParties.getApplicantContactParties())) {
            message.append(APPLICANT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(contactParties.getRespondent())) {
            message.append(RESPONDENT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(contactParties.getRepresentativeContactParties())) {
            message.append(REPRESENTATIVE + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(contactParties.getTribunal())) {
            message.append(TRIBUNAL + COMMA_SPACE);
        }

        return message.substring(0, message.length() - 2);
    }

    public static String generateSimpleErrorMessage(final List<String> errors) {
        final StringBuilder message = new StringBuilder(100);

        message.append("A notification could not be sent to: ");
        errors.forEach(e -> message.append(e).append(COMMA_SPACE));

        return message.substring(0, message.length() - 2);
    }

    public static String generateSimpleErrorMessageDocumentSave(final Document document, String errorMessage) {
        if (document.getFilename() != null
            && !document.getFilename().isEmpty()) {
            log.error("Document entity with filename {} could not be saved: {}", document.getFilename(), errorMessage);
            return FAILED_SAVING_DOCUMENT_TO_DATABASE + document.getFilename();
        } else {
            log.error("Document entity has no filename {}", errorMessage);
            return FAILED_SAVING_DOCUMENT_WITH_NO_FILENAME_TO_DATABASE;
        }
    }

    public static String generateSimpleMessageBundleCreation(final CicCase cicCase) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");

        if (cicCase.getRepNotificationResponse() != null) {
            message.append(REPRESENTATIVE + COMMA_SPACE);
        }
        if (cicCase.getResNotificationResponse() != null) {
            message.append(RESPONDENT + COMMA_SPACE);
        }
        if (cicCase.getAppNotificationResponse() != null) {
            message.append(APPLICANT + COMMA_SPACE);
        }
        return message.substring(0, message.length() - 2);
    }
}
