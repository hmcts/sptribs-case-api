package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseStay;
import uk.gov.hmcts.sptribs.caseworker.model.StayReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerStayTheCase.CASEWORKER_STAY_THE_CASE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerStayTheCaseTest {

    @InjectMocks
    private CaseworkerStayTheCase caseworkerStayTheCase;

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
    void shouldSuccessfullyListRecording() {
        //Given
        final CaseData caseData = caseData();
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
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerStayTheCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerStayTheCase.stayed(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCaseStay()).isNotNull();
        assertThat(stayedResponse).isNotNull();
        CaseStay stay = response.getData().getCaseStay();
        Assertions.assertEquals(StayReason.AWAITING_OUTCOME_OF_LINKED_CASE, stay.getStayReason());
        assertThat(stay.getAdditionalDetail()).isNotNull();
        assertThat(stay.getExpirationDate()).isNotNull();
        assertThat(stay.getFlagType()).isNull();
    }

}
