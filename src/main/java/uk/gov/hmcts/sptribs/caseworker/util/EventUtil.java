package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeRespondent;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;

public final class EventUtil {

    private EventUtil() {
    }

    public static List<String> checkRecipient(CaseData data) {
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeRespondent(data)) {
            errors.add(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
        } else if ((data.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || data.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())) {
            errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
        }
        return errors;
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
            recipients.append(cicCase.getNotifyPartySubject() + ", ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            recipients.append(cicCase.getNotifyPartyRespondent() + ", ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            recipients.append(cicCase.getNotifyPartyRepresentative() + ", ");
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            recipients.append(getNotifyPartyApplicant + ", ");
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


    public static String parseHyphen(String selectedVenue, int index) {
        String[] values = (selectedVenue != null) ? Arrays.stream(selectedVenue.split(HYPHEN))
            .map(String::trim)
            .toArray(String[]::new) : null;
        return values != null && values.length > 0 ? values[index] : null;
    }

    public static String getMainContent(DecisionTemplate decision) {
        String mainContent = "";
        if (decision.equals(DecisionTemplate.ELIGIBILITY)) {
            mainContent = DocmosisTemplateConstants.ELIGIBILITY_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.QUANTUM)) {
            mainContent = DocmosisTemplateConstants.QUANTUM_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.RULE_27)) {
            mainContent = DocmosisTemplateConstants.RULE27_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.ME_DMI_REPORTS)) {
            mainContent = DocmosisTemplateConstants.ME_DMI_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.ME_JOINT_INSTRUCTION)) {
            mainContent = DocmosisTemplateConstants.ME_JOINT_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.STRIKE_OUT_WARNING)) {
            mainContent = DocmosisTemplateConstants.STRIKE_OUT_WARNING_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.STRIKE_OUT_DECISION_NOTICE)) {
            mainContent = DocmosisTemplateConstants.STRIKE_OUT_NOTICE_MAIN_CONTENT;
        } else if (decision.equals(DecisionTemplate.PRO_FORMA_SUMMONS)) {
            mainContent = DocmosisTemplateConstants.PRO_FORMA_MAIN_CONTENT;
        }
        return mainContent;
    }

    public static String getOrderMainContent(OrderTemplate order) {
        String mainContent = "";
        if (order == OrderTemplate.CIC7_ME_DMI_REPORTS) {
            mainContent = DocmosisTemplateConstants.ME_DMI_MAIN_CONTENT;
        } else if (order == OrderTemplate.CIC8_ME_JOINT_INSTRUCTION) {
            mainContent = DocmosisTemplateConstants.ME_JOINT_MAIN_CONTENT;
        } else if (order == OrderTemplate.CIC10_STRIKE_OUT_WARNING) {
            mainContent = DocmosisTemplateConstants.STRIKE_OUT_WARNING_MAIN_CONTENT;
        } else if (order == OrderTemplate.CIC13_PRO_FORMA_SUMMONS) {
            mainContent = DocmosisTemplateConstants.PRO_FORMA_MAIN_CONTENT;
        } else if (order == OrderTemplate.CIC3_RULE_27) {
            mainContent = DocmosisTemplateConstants.RULE27_MAIN_CONTENT;
        }
        return mainContent;
    }

    public static List<ListValue<PanelMember>> getPanelMembers(DynamicList judicialUsersDynamicList) {
        PanelMember panelMember = PanelMember.builder()
            .name(judicialUsersDynamicList)
            .build();
        List<ListValue<PanelMember>> listValues = new ArrayList<>();

        ListValue<PanelMember> listValue = ListValue
            .<PanelMember>builder()
            .id("1")
            .value(panelMember)
            .build();

        listValues.add(listValue);
        return listValues;
    }


}
