package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.CaseStayedNotification;

import java.time.LocalDate;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_STAY_THE_CASE;

@ExtendWith(MockitoExtension.class)
class CaseworkerStayTheCaseTest {

    @InjectMocks
    private CaseworkerStayTheCase caseworkerStayTheCase;

    @Mock
    private CaseStayedNotification caseStayedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerStayTheCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_STAY_THE_CASE);
    }

    @Test
    void shouldSuccessfullyClearPreviousRemoveStayFromCase() {
        //Given
        final CaseData caseData = caseData();
        CaseStay caseStay = new CaseStay();
        caseStay.setStayReason(StayReason.AWAITING_OUTCOME_OF_LINKED_CASE);
        caseStay.setAdditionalDetail("some detail");
        caseStay.setFlagType(null);
        caseStay.setExpirationDate(LocalDate.now());
        caseData.setCaseStay(caseStay);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(State.CaseManagement);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerStayTheCase.aboutToStart(caseDetails);


        //Then
        assertThat(response.getData().getCaseStay().getStayReason()).isNull();
    }

    @Test
    void shouldSuccessfullyStayTheCase() {
        //Given
        final CaseData caseData = caseData();
        CicCase cicCase = new CicCase();
        cicCase.setSubjectCIC(Set.of(SubjectCIC.SUBJECT));
        cicCase.setApplicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC));
        cicCase.setRepresentativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);
        caseData.setNote("This is a test note");
        CaseStay caseStay = new CaseStay();
        caseStay.setStayReason(StayReason.AWAITING_OUTCOME_OF_LINKED_CASE);
        caseStay.setAdditionalDetail("some detail");
        caseStay.setFlagType(null);
        caseStay.setExpirationDate(LocalDate.now());
        caseData.setCaseStay(caseStay);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        doNothing().when(caseStayedNotification).sendToSubject(any(CaseData.class), eq(null));
        doNothing().when(caseStayedNotification).sendToApplicant(any(CaseData.class), eq(null));
        doNothing().when(caseStayedNotification).sendToRepresentative(any(CaseData.class), eq(null));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerStayTheCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerStayTheCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCaseStay()).isNotNull();
        assertThat(stayedResponse).isNotNull();
        CaseStay stay = response.getData().getCaseStay();
        Assertions.assertEquals(StayReason.AWAITING_OUTCOME_OF_LINKED_CASE, stay.getStayReason());
        assertThat(stay.getAdditionalDetail()).isNotNull();
        assertThat(stay.getExpirationDate()).isNotNull();
        assertThat(stay.getFlagType()).isNull();
    }

    @Test
    void shouldNotSendNotificationIfApplicantSubjectRepresentativeSetIsEmpty() {
        //Given
        final CaseData caseData = caseData();
        caseData.getCicCase().setSubjectCIC(emptySet());
        caseData.getCicCase().setSubjectCIC(emptySet());
        caseData.getCicCase().setSubjectCIC(emptySet());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        //When
        caseworkerStayTheCase.submitted(details, details);

        //Then
        verifyNoInteractions(caseStayedNotification);
    }

    @Test
    void shouldNotSendNotificationIfApplicantSubjectRepresentativeSetIsNull() {
        //Given
        final CaseData caseData = caseData();
        caseData.getCicCase().setSubjectCIC(null);
        caseData.getCicCase().setSubjectCIC(null);
        caseData.getCicCase().setSubjectCIC(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        //When
        caseworkerStayTheCase.submitted(details, details);

        //Then
        verifyNoInteractions(caseStayedNotification);
    }
}
