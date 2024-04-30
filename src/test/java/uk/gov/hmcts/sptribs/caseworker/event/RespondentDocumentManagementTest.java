package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;

@ExtendWith(MockitoExtension.class)
class RespondentDocumentManagementTest {

    @InjectMocks
    private RespondentDocumentManagement respondentDocumentManagement;


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        respondentDocumentManagement.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(RESPONDENT_DOCUMENT_MANAGEMENT);
    }

    @Test
    void shouldSuccessfullyAddDocumentInAboutToSubmit() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocumentUpload(getCaseworkerCICDocumentUploadList("file.pdf"))
            .build();
        caseData.setNewDocManagement(documentManagement);

        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            respondentDocumentManagement.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getNewDocManagement().getCaseworkerCICDocument()).isEmpty();
        assertThat(response.getData().getNewDocManagement().getCaseworkerCICDocumentUpload()).isEmpty();
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument()).hasSize(1);
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocumentUpload()).isEmpty();
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().get(0).getValue().getDocumentCategory())
            .isEqualTo(DocumentType.LINKED_DOCS);
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().get(0).getValue().getDocumentEmailContent())
            .isEqualTo("some email content");
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().get(0).getValue().getDocumentLink().getFilename())
            .isEqualTo("file.pdf");
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().get(0).getValue().getDate()).isNotNull();
    }

    @Test
    void shouldSuccessfullySubmit() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        SubmittedCallbackResponse response = respondentDocumentManagement.submitted(updatedCaseDetails, beforeDetails);
        assertThat(response.getConfirmationHeader()).isEqualTo("# Case Updated");
    }

}
