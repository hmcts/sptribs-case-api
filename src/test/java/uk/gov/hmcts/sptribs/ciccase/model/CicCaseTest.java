package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.CicCaseFieldsUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;

class CicCaseTest {

    @Test
    void shouldRemoveRepresentative() {
        //When
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        Set<ContactPartiesCIC> contactPartiesCICS = new HashSet<>();
        contactPartiesCICS.add(ContactPartiesCIC.REPRESENTATIVETOCONTACT);
        contactPartiesCICS.add(ContactPartiesCIC.RESPONDENTTOCONTACT);
        final CicCase cicCase = CicCase.builder()
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .representativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .contactPartiesCIC(contactPartiesCICS)
            .hearingNotificationParties(parties)
            .build();

        final CaseData data = CaseData.builder()
            .cicCase(cicCase)
            .build();

        //When
        CicCaseFieldsUtil.removeRepresentative(data);
        //Then
        assertThat(cicCase.getRepresentativeFullName()).isEmpty();
    }

    @Test
    void shouldRemoveApplicant() {
        //When
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .applicantAddress(APPLICANT_ADDRESS)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .applicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC))
            .hearingNotificationParties(parties)
            .build();

        final CaseData data = CaseData.builder()
            .cicCase(cicCase)
            .build();

        //When
        CicCaseFieldsUtil.removeApplicant(data);
        //Then
        assertThat(cicCase.getApplicantFullName()).isEmpty();
    }

    @Test
    void shouldCalculateFirstOrderDueDate() {
        //When
        LocalDate now = LocalDate.now();
        DateModel dateModel1 = DateModel.builder().dueDate(now).build();
        ListValue<DateModel> dateModelListValue1 = new ListValue<>();
        dateModelListValue1.setValue(dateModel1);
        ListValue<Order> orderListValue1 = new ListValue<>();
        Order order1 = Order.builder().dueDateList(List.of(dateModelListValue1)).build();
        orderListValue1.setValue(order1);
        List<ListValue<Order>> orderList = new ArrayList<>();
        orderList.add(orderListValue1);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DateModel dateModel2 = DateModel.builder().dueDate(tomorrow).build();
        ListValue<DateModel> dateModelListValue2 = new ListValue<>();
        dateModelListValue2.setValue(dateModel2);
        ListValue<Order> orderListValue2 = new ListValue<>();
        Order order2 = Order.builder().dueDateList(List.of(dateModelListValue2)).build();
        orderListValue2.setValue(order2);
        orderList.add(orderListValue2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateModel dateModel3 = DateModel.builder()
            .orderMarkAsCompleted(Set.of(GetAmendDateAsCompleted.MARKASCOMPLETED))
            .dueDate(yesterday)
            .build();
        ListValue<DateModel> dateModelListValue3 = new ListValue<>();
        dateModelListValue3.setValue(dateModel3);
        ListValue<Order> orderListValue3 = new ListValue<>();
        Order order3 = Order.builder().dueDateList(List.of(dateModelListValue3)).build();
        orderListValue3.setValue(order3);
        orderList.add(orderListValue3);
        final CicCase cicCase = CicCase.builder()
            .orderList(orderList)
            .build();

        //When
        LocalDate result = CicCaseFieldsUtil.calculateFirstDueDate(cicCase.getOrderList());

        //Then
        assertThat(result).isEqualTo(now);
    }

    @ParameterizedTest
    @EnumSource(CaseSubcategory.class)
    void shouldUseApplicantNameForSubjectWithMinorSubcategoryAndFatalSubcategory(CaseSubcategory caseSubcategory) {
        final CicCase cicCase = CicCase.builder()
            .caseSubcategory(caseSubcategory)
            .applicantFullName("Jane Doe")
            .build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        if (caseSubcategory == CaseSubcategory.FATAL
            || caseSubcategory == CaseSubcategory.MINOR) {
            assertThat(CicCaseFieldsUtil.useApplicantNameForSubject(
                caseData.getCicCase().getCaseSubcategory(), caseData.getCicCase().getApplicantFullName()
            )).isTrue();
        } else {
            assertThat(CicCaseFieldsUtil.useApplicantNameForSubject(
                caseData.getCicCase().getCaseSubcategory(), caseData.getCicCase().getApplicantFullName()
            )).isFalse();
        }

        final CicCase cicCaseNoApplicantName = CicCase.builder()
            .caseSubcategory(caseSubcategory)
            .build();

        final CaseData caseDataNoApplicantName = CaseData.builder()
            .cicCase(cicCaseNoApplicantName)
            .build();

        if (caseSubcategory == CaseSubcategory.FATAL
            || caseSubcategory == CaseSubcategory.MINOR) {
            assertThat(CicCaseFieldsUtil.useApplicantNameForSubject(
                caseDataNoApplicantName.getCicCase().getCaseSubcategory(), caseDataNoApplicantName.getCicCase().getApplicantFullName()
            )).isFalse();
        }
    }
}
