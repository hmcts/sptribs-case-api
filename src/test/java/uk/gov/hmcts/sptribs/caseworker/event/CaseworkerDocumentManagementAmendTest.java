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
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementAmendDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementSelectDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_AMEND;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithUUID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicMultiSelectDocumentListWithUUID;

@ExtendWith(MockitoExtension.class)
class CaseworkerDocumentManagementAmendTest {

    @InjectMocks
    private CaseworkerDocumentManagementAmend caseworkerDocumentManagementAmend;

    @InjectMocks
    private DocumentManagementSelectDocuments selectCaseDocuments;

    @InjectMocks
    private DocumentManagementAmendDocuments amendCaseDocuments;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseworkerDocumentManagementAmend.setCaseFileViewAndDocumentManagementEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDocumentManagementAmend.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND);
    }

    @Test
    void shouldSuccessfullySelectDocument() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        UUID uuid = UUID.randomUUID();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocument(getCaseworkerCICDocumentListWithUUID(uuid))
            .documentList(getDynamicMultiSelectDocumentListWithUUID(uuid))
            .build();
        caseData.setDocManagement(documentManagement);
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When

        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            selectCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(midResponse).isNotNull();
        assertThat(midResponse.getData().getDocManagement().getSelectedDocuments()).isNotEmpty();
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldNotConfigureMaintainLinkCaseIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDocumentManagementAmend.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND);
    }

    @Test
    void shouldSuccessfullyAddDocument() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocument(getCaseworkerCICDocumentList())
            .build();
        caseData.setDocManagement(documentManagement);
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When


        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(documentMgmtResponse).isNotNull();
    }

}
