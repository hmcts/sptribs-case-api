package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;

public final class EventUtil {

    private EventUtil() {
    }


    public static String getId(String selectedDraft) {
        String[] values = (selectedDraft != null) ? Arrays.stream(selectedDraft.split(HYPHEN))
            .map(String::trim)
            .toArray(String[]::new) : null;
        return values != null && values.length > 0 ? values[0] : null;
    }

    public static String getRecipients(final CicCase cicCase) {
        final StringBuilder recipients = new StringBuilder(100);
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            recipients.append("Subject, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            recipients.append("Respondent, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            recipients.append("Representative, ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            recipients.append("Applicant, ");
        }
        if (recipients.length() > 0) {
            return recipients.substring(0, recipients.length() - 2);
        }
        return null;
    }

    public static Set<NotificationParties> getNotificationParties(final CicCase cicCase) {
        Set<NotificationParties> parties = new HashSet<>();
        if (!CollectionUtils.isEmpty(cicCase.getSubjectCIC())) {
            parties.add(NotificationParties.SUBJECT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getApplicantCIC())) {
            parties.add(NotificationParties.APPLICANT);
        }
        if (!CollectionUtils.isEmpty(cicCase.getRepresentativeCIC())) {
            parties.add(NotificationParties.REPRESENTATIVE);
        }
        return parties;
    }
}
