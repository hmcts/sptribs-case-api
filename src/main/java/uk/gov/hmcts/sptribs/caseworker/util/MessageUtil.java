package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;

import java.util.Set;

import static java.lang.String.format;

public final class MessageUtil {
    private static final String REPRESENTATIVE = "Representative";
    private static final String RESPONDENT = "Respondent";
    private static final String SUBJECT = "Subject";

    private static final String TRIBUNAL = "Tribunal";

    private static final String COMMA_SPACE = ", ";

    private MessageUtil() {
    }

    public static String generateSimpleMessage(String header, String footer) {
        String message = format("# %s", header);
        if (StringUtils.hasText(footer)) {
            StringBuilder sb = new StringBuilder(message);
            sb.append(format(" %n## %s", footer));
            message = sb.toString();
        }
        return message;
    }

    public static String generateSimpleMessage(final CicCase cicCase, String header, String footer) {
        final String notificationMessage = generateSimpleMessage(cicCase);
        String message = format("# %s %n## %s", header, notificationMessage);
        if (StringUtils.hasText(footer)) {
            StringBuilder sb = new StringBuilder(message);
            sb.append(format(" %n## %s", footer));
            message = sb.toString();
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
        return message.substring(0, message.length() - 2);
    }

    public static String generateSimpleMessage(final ContactParties contactParties) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");

        if (!CollectionUtils.isEmpty(contactParties.getSubjectContactParties())) {
            message.append(SUBJECT + COMMA_SPACE);
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
}
