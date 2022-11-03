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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseReinstate;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocumentsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.ReinstateCase.CASEWORKER_REINSTATE_CASE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;


@ExtendWith(MockitoExtension.class)
class ReinstateCaseTest {

    @InjectMocks
    private ReinstateCase reinstateCase;


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        reinstateCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REINSTATE_CASE);
    }

    @Test
    void shouldSuccessfullyReinstateTheCase() {
        //Given
        final CaseData caseData = caseData();
        final CaseReinstate caseReinstate = new CaseReinstate();
        caseReinstate.setReinstateReason(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR);
        caseReinstate.setAdditionalDetail("some detail");
        caseData.setCaseReinstate(caseReinstate);
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        CaseDocumentsCIC caseDocumentsCIC = CaseDocumentsCIC.builder().applicantDocumentsUploaded(List.of(documentListValue)).build();
        final CicCase cicCase = CicCase.builder()
            .reinstateDocuments(caseDocumentsCIC)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            reinstateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse responseReinstate =
            reinstateCase.reinstated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(responseReinstate).isNotNull();
        assertThat(response.getData().getCaseReinstate()).isNotNull();
        assertThat(response.getState()).isEqualTo(State.CaseManagement);
        CaseReinstate responseCaseReinstate = response.getData().getCaseReinstate();
        Assertions.assertEquals(ReinstateReason.CASE_HAD_BEEN_CLOSED_IN_ERROR, responseCaseReinstate.getReinstateReason());
        assertThat(responseCaseReinstate.getAdditionalDetail()).isNotNull();
        assertThat(response.getData().getCicCase().getReinstateDocuments().getApplicantDocumentsUploaded()
            .get(0).getValue().getDocumentEmailContent()).isNotNull();
        assertThat(response.getData().getCicCase().getReinstateDocuments().getApplicantDocumentsUploaded()
            .get(0).getValue().getDocumentLink()).isNotNull();

    }


}
