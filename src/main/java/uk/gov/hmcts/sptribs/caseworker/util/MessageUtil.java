package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

public final class MessageUtil {
    private static final String REPRESENTATIVE = "Representative";
    private static final String RESPONDENT = "Respondent";
    private static final String SUBJECT = "Subject";

    private static final String COMMA_SPACE = ", ";

    private MessageUtil() {
    }

    public static String getPostMessage(final CicCase cicCase) {
        Set<HasLabel> allParties = new HashSet<>();
        if (null != cicCase.getNotifyPartySubject()) {
            allParties.addAll(cicCase.getNotifyPartySubject());
        }
        if (null != cicCase.getNotifyPartyRepresentative()) {
            allParties.addAll(cicCase.getNotifyPartyRepresentative());
        }
        return getPostMessage(cicCase, allParties);
    }

    public static String getPostMessage(final CicCase cicCase, final Set<? extends HasLabel> parties) {
        boolean post = false;
        StringBuilder postMessage = new StringBuilder(100);
        postMessage.append("It will be sent via post to: ");
        if (parties.stream().anyMatch(party -> SUBJECT.equals(party.getLabel()))
            && ContactPreferenceType.POST == cicCase.getContactPreferenceType()
            && !ObjectUtils.isEmpty(cicCase.getAddress())) {
            postMessage.append(SUBJECT + COMMA_SPACE);
            post = true;
        }
        if (parties.stream().anyMatch(party -> REPRESENTATIVE.equals(party.getLabel()))
            && ContactPreferenceType.POST == cicCase.getRepresentativeContactDetailsPreference()
            && !ObjectUtils.isEmpty(cicCase.getRepresentativeAddress())) {
            postMessage.append(REPRESENTATIVE + COMMA_SPACE);
            post = true;
        }
        return post ? postMessage.substring(0, postMessage.length() - 2) : null;
    }

    public static String getEmailMessage(final CicCase cicCase) {
        Set<HasLabel> allParties = new HashSet<>();
        if (null != cicCase.getNotifyPartySubject()) {
            allParties.addAll(cicCase.getNotifyPartySubject());
        }
        if (null != cicCase.getNotifyPartyRespondent()) {
            allParties.addAll(cicCase.getNotifyPartyRespondent());
        }
        if (null != cicCase.getNotifyPartyRepresentative()) {
            allParties.addAll(cicCase.getNotifyPartyRepresentative());
        }
        return getEmailMessage(cicCase, allParties);
    }

    public static String getEmailMessage(final CicCase cicCase, final Set<? extends HasLabel> parties) {
        final StringBuilder messageLine = new StringBuilder(100);
        messageLine.append(" A notification will be sent to: ");
        boolean email = false;
        if (parties.stream().anyMatch(party -> SUBJECT.equals(party.getLabel()))
            && StringUtils.hasText(cicCase.getEmail())) {
            messageLine.append(SUBJECT + COMMA_SPACE);
            email = true;
        }
        if (parties.stream().anyMatch(party -> RESPONDENT.equals(party.getLabel()))) {
            messageLine.append(RESPONDENT + COMMA_SPACE);
            email = true;
        }
        if (parties.stream().anyMatch(party -> REPRESENTATIVE.equals(party.getLabel()))
            && StringUtils.hasText(cicCase.getRepresentativeEmailAddress())) {
            messageLine.append(REPRESENTATIVE + COMMA_SPACE);
            email = true;
        }
        return email ? messageLine.substring(0, messageLine.length() - 2) : null;
    }

    public static String generateWholeMessage(final CicCase cicCase, final String header, String footer) {
        final String emailMessage = getEmailMessage(cicCase);
        final String postMessage = getPostMessage(cicCase);
        return formatWholeMessage(header, footer, emailMessage, postMessage);
    }

    @SafeVarargs
    public static String generateWholeMessage(final CicCase cicCase, final String header, String footer,
                                              Set<? extends HasLabel>... parties) {
        Set<HasLabel> allParties = new HashSet<>();
        Arrays.asList(parties).stream().filter(party -> null != party).forEach(party -> allParties.addAll(party));
        final String emailMessage = getEmailMessage(cicCase, allParties);
        final String postMessage = getPostMessage(cicCase, allParties);
        return formatWholeMessage(header, footer, emailMessage, postMessage);
    }

    private static String formatWholeMessage(String header, String footer, String emailMessage, String postMessage) {
        String message = "";
        if (null != postMessage && null != emailMessage) {
            message = format("# %s %n## %s %n## %s", header, emailMessage, postMessage);
        } else if (null != emailMessage) {
            message = format("# %s %n## %s", header, emailMessage);

        } else if (null != postMessage) {
            message = format("# %s %n ## %s ", header, postMessage);
        }
        if (StringUtils.hasText(footer)) {
            message += format(" %n## %s", footer);
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
