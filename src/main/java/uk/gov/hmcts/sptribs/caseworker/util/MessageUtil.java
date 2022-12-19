package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;

import java.util.Set;

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
            && !ObjectUtils.isEmpty(cicCase.getAddress())
            && !ObjectUtils.isEmpty(cicCase.getAddress().getPostCode())) {
            postMessage.append(SUBJECT);
            post = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
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
            && !ObjectUtils.isEmpty(cicCase.getAddress())
            && !ObjectUtils.isEmpty(cicCase.getAddress().getPostCode())) {
            postMessage.append(SUBJECT);
            post = true;
        }
        if (parties.contains(NotificationParties.REPRESENTATIVE)
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
        messageLine.append(" A notification will be sent via email to: ");
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
        messageLine.append(" A notification will be sent via email to: ");
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
}
