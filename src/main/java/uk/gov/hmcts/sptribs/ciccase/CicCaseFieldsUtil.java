package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CicCaseFieldsUtil {

    private CicCaseFieldsUtil() {}

    public static void calculateAndSetIsCaseInTime(CaseData data) {
        data.getCicCase().setIsCaseInTime(YesOrNo.NO);

        LocalDate initialCicaDecisionDatePlus90Days = null;
        if (data.getCicCase().getInitialCicaDecisionDate() != null) {
            initialCicaDecisionDatePlus90Days = data.getCicCase().getInitialCicaDecisionDate().plusDays(90);
        }

        if (initialCicaDecisionDatePlus90Days != null
            && !data.getCicCase().getCaseReceivedDate().isAfter(initialCicaDecisionDatePlus90Days)) {
            data.getCicCase().setIsCaseInTime(YesOrNo.YES);
        }
    }

    public static LocalDate calculateFirstDueDate(List<ListValue<Order>> orderList) {
        LocalDate compare = LocalDate.MAX;

        if (!CollectionUtils.isEmpty(orderList)) {
            for (ListValue<Order> orderListValue : orderList) {
                if (!CollectionUtils.isEmpty(orderListValue.getValue().getDueDateList())) {
                    compare = findEarliestDate(orderListValue.getValue().getDueDateList(), compare);
                }
            }

            if (compare.isBefore(LocalDate.MAX)) {
                return compare;
            }
        }

        return null;
    }

    private static LocalDate findEarliestDate(List<ListValue<DateModel>> dueDateList, LocalDate compare) {
        LocalDate earliestDate = compare;
        for (ListValue<DateModel> dateModelListValue : dueDateList) {
            if ((dateModelListValue.getValue().getOrderMarkAsCompleted() == null
                || !dateModelListValue.getValue().getOrderMarkAsCompleted().contains(GetAmendDateAsCompleted.MARKASCOMPLETED))
                && dateModelListValue.getValue().getDueDate().isBefore(compare)) {
                earliestDate = dateModelListValue.getValue().getDueDate();
            }
        }
        return earliestDate;
    }

    public static String getSelectedHearingToCancel(DynamicList hearingList) {
        return hearingList != null ? hearingList.getValue().getLabel() : null;
    }

    public static void removeRepresentative(CaseData data) {
        Set<RepresentativeCIC> representativeCIC = data.getCicCase().getRepresentativeCIC();
        if (representativeCIC != null) {
            representativeCIC = new HashSet<>();
        }
        Set<RepresentativeCIC> notifyPartyRepresentative = data.getCicCase().getNotifyPartyRepresentative();
        if (notifyPartyRepresentative != null) {
            notifyPartyRepresentative = new HashSet<>();
        }
        Set<NotificationParties> hearingNotificationParties = data.getCicCase().getHearingNotificationParties();
        if (hearingNotificationParties != null) {
            hearingNotificationParties.remove(NotificationParties.REPRESENTATIVE);
        }
        Set<ContactPartiesCIC> contactPartiesCIC = data.getCicCase().getContactPartiesCIC();
        if (contactPartiesCIC != null) {
            Set<ContactPartiesCIC> temp = new HashSet<>();
            for (ContactPartiesCIC partyCIC : contactPartiesCIC) {
                if (partyCIC != ContactPartiesCIC.REPRESENTATIVETOCONTACT) {
                    temp.add(partyCIC);
                }
            }
            contactPartiesCIC = temp;
        }

        data.getCicCase().setRepresentativeFullName("");
        data.getCicCase().setRepresentativeOrgName("");
        data.getCicCase().setRepresentativeReference("");
        data.getCicCase().setRepresentativeAddress(new AddressGlobalUK());
        data.getCicCase().setRepresentativePhoneNumber("");
        data.getCicCase().setRepresentativeEmailAddress("");
    }

    public static void removeApplicant(CaseData data) {
        Set<ApplicantCIC> applicantCIC = data.getCicCase().getApplicantCIC();
        if (applicantCIC != null) {
            applicantCIC = new HashSet<>();
        }
        Set<ApplicantCIC> notifyPartyApplicant = data.getCicCase().getNotifyPartyApplicant();
        if (notifyPartyApplicant != null) {
            notifyPartyApplicant = new HashSet<>();
        }
        Set<NotificationParties> hearingNotificationParties = data.getCicCase().getHearingNotificationParties();
        if (hearingNotificationParties != null) {
            hearingNotificationParties.remove(NotificationParties.APPLICANT);
        }

        data.getCicCase().setApplicantFullName("");
        data.getCicCase().setApplicantAddress(new AddressGlobalUK());
        data.getCicCase().setApplicantPhoneNumber("");
        data.getCicCase().setApplicantEmailAddress("");
    }

    public static boolean useApplicantNameForSubject(CaseSubcategory caseSubcategory, String applicantFullName) {
        return (caseSubcategory == CaseSubcategory.FATAL
            || caseSubcategory == CaseSubcategory.MINOR)
            && (applicantFullName != null);
    }
}
