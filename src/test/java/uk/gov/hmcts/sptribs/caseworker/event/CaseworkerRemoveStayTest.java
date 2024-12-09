package uk.gov.hmcts.sptribs.caseworker.event;

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
import uk.gov.hmcts.sptribs.caseworker.model.RemoveCaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayRemoveReason;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseUnstayedNotification;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_REMOVE_STAY;

@ExtendWith(MockitoExtension.class)
class CaseworkerRemoveStayTest {

    @InjectMocks
    private CaseworkerRemoveStay caseworkerRemoveStay;

    @Mock
    private CaseUnstayedNotification caseUnstayedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerRemoveStay.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REMOVE_STAY);
    }

    @Test
    void shouldSuccessfullyClearPreviousRemoveStayFromCase() {
        //Given
        final CaseData caseData = caseData();
        RemoveCaseStay removeCaseStay = new RemoveCaseStay();
        removeCaseStay.setStayRemoveReason(StayRemoveReason.OTHER);
        removeCaseStay.setAdditionalDetail("some detail");
        caseData.setRemoveCaseStay(removeCaseStay);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(State.CaseStayed);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveStay.aboutToStart(caseDetails);


        //Then
        assertThat(response.getData().getRemoveCaseStay().getStayRemoveReason()).isNull();
    }

    @Test
    void shouldSuccessfullyRemoveStayFromCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        cicCase.setSubjectCIC(Collections.emptySet());
        cicCase.setApplicantCIC(Collections.emptySet());
        cicCase.setRepresentativeCIC(Collections.emptySet());
        caseData.setCicCase(cicCase);
        RemoveCaseStay removeCaseStay = new RemoveCaseStay();
        removeCaseStay.setStayRemoveReason(StayRemoveReason.OTHER);
        removeCaseStay.setAdditionalDetail("some detail");
        caseData.setRemoveCaseStay(removeCaseStay);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveStay.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerRemoveStay.submitted(updatedCaseDetails, beforeDetails);


        //Then
        assert (response.getState().equals(State.CaseManagement));
        assertThat(stayedResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyRemoveStayFromCaseWithNotify() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        cicCase.setSubjectCIC(Set.of(SubjectCIC.SUBJECT));
        cicCase.setApplicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC));
        cicCase.setRepresentativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.setCicCase(cicCase);
        RemoveCaseStay removeCaseStay = new RemoveCaseStay();
        removeCaseStay.setStayRemoveReason(StayRemoveReason.OTHER);
        removeCaseStay.setAdditionalDetail("some detail");
        caseData.setRemoveCaseStay(removeCaseStay);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        doNothing().when(caseUnstayedNotification).sendToSubject(any(CaseData.class), eq(null));
        doNothing().when(caseUnstayedNotification).sendToApplicant(any(CaseData.class), eq(null));
        doNothing().when(caseUnstayedNotification).sendToRepresentative(any(CaseData.class), eq(null));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveStay.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerRemoveStay.submitted(updatedCaseDetails, beforeDetails);


        //Then
        assert (response.getState().equals(State.CaseManagement));
        assertThat(stayedResponse).isNotNull();
    }

}
