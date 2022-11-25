package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static StringBuilder getPostMessage(final CicCase cicCase) {
        boolean post = false;
        StringBuilder postMessage = new StringBuilder(100);
        postMessage.append("It will be sent via post to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())
            && !ObjectUtils.isEmpty(cicCase.getAddress())) {
            postMessage.append("Subject, ");
            post = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && !ObjectUtils.isEmpty(cicCase.getRepresentativeAddress())) {
            postMessage.append("Representative, ");
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
            messageLine.append("Subject, ");
            cicCase.setNotifyPartySubject(null);
            email = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine.append("Respondent, ");
            cicCase.setNotifyPartyRespondent(null);
            email = true;
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            messageLine.append("Representative, ");
            cicCase.setNotifyPartyRepresentative(null);
            email = true;
        }
        if (email) {
            return messageLine;
        }
        return null;
    }
}
