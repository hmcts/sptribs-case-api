package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;

import java.util.Set;

import static java.lang.String.format;

public final class MessageUtil {
    private static final String REPRESENTATIVE = "Representative, ";
    private static final String RESPONDENT = "Respondent, ";
    private static final String SUBJECT = "Subject, ";

    private MessageUtil() {
    }

    public static StringBuilder getPostMessage(final CicCase cicCase) {
        boolean post = false;
        StringBuilder postMessage = new StringBuilder(100);
        postMessage.append("It will be sent via post to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && ContactPreferenceType.POST == cicCase.getContactPreferenceType()
            && !ObjectUtils.isEmpty(cicCase.getAddress())) {
            postMessage.append(SUBJECT);
            post = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && ContactPreferenceType.POST == cicCase.getRepresentativeContactDetailsPreference()
            && !ObjectUtils.isEmpty(cicCase.getRepresentativeAddress())) {
            postMessage.append(REPRESENTATIVE);
            post = true;
        }
        if (post) {
            return postMessage;
        }
        return null;
    }

    public static StringBuilder getPostMessage(final CicCase cicCase, final Set<NotificationParties> parties) {
        boolean post = false;
        StringBuilder postMessage = new StringBuilder(100);
        postMessage.append("It will be sent via post to: ");
        if (parties.contains(NotificationParties.SUBJECT)
            && ContactPreferenceType.POST == cicCase.getContactPreferenceType()
            && !ObjectUtils.isEmpty(cicCase.getAddress())) {
            postMessage.append(SUBJECT);
            post = true;
        }
        if (parties.contains(NotificationParties.REPRESENTATIVE)
            && ContactPreferenceType.POST == cicCase.getRepresentativeContactDetailsPreference()
            && !ObjectUtils.isEmpty(cicCase.getRepresentativeAddress())) {
            postMessage.append(REPRESENTATIVE);
            post = true;
        }
        if (post) {
            return postMessage;
        }
        return null;
    }

    public static StringBuilder getEmailMessage(final CicCase cicCase) {
        final StringBuilder messageLine = new StringBuilder(100);
        messageLine.append(" A notification will be sent to: ");
        boolean email = false;
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && StringUtils.hasText(cicCase.getEmail())) {
            messageLine.append(SUBJECT);
            email = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine.append(RESPONDENT);
            email = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            messageLine.append(REPRESENTATIVE);
            email = true;
        }
        if (email) {
            return messageLine;
        }
        return null;
    }

    public static StringBuilder getEmailMessage(final CicCase cicCase, final Set<NotificationParties> parties) {
        final StringBuilder messageLine = new StringBuilder(100);
        messageLine.append(" A notification will be sent to: ");
        boolean email = false;
        if (parties.contains(NotificationParties.SUBJECT)
            && StringUtils.hasText(cicCase.getEmail())) {
            messageLine.append(SUBJECT);
            email = true;
        }
        if (parties.contains(NotificationParties.RESPONDENT)) {
            messageLine.append(RESPONDENT);
            email = true;
        }
        if (parties.contains(NotificationParties.REPRESENTATIVE)
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            messageLine.append(REPRESENTATIVE);
            email = true;
        }
        if (email) {
            return messageLine;
        }
        return null;
    }


    public static String generateWholeMessage(final CicCase cicCase) {
        final StringBuilder emailMessage = getEmailMessage(cicCase);

        StringBuilder postMessage = getPostMessage(cicCase);
        String message = "";
        if (null != postMessage && null != emailMessage) {
            message = format("# Final decision notice issued   %n"
                + " %s  %n  %s", emailMessage.substring(0, emailMessage.length() - 2), postMessage.substring(0, postMessage.length() - 2));
        } else if (null != emailMessage) {
            message = format("# Final decision notice issued %n ## "
                + " %s ", emailMessage.substring(0, emailMessage.length() - 2));

        } else if (null != postMessage) {
            message = format("# Final decision notice issued  %n ## "
                + " %s ", postMessage.substring(0, postMessage.length() - 2));
        }
        return message;
    }

    public static String generateIssueDecisionMessage(final CicCase cicCase) {
        final StringBuilder message = new StringBuilder(100);
        message.append(format("# Decision notice issued %n ## ")).append("A notification has been sent to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            message.append(SUBJECT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            message.append(RESPONDENT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            message.append(REPRESENTATIVE);
        }

        return message.toString();
    }
}
