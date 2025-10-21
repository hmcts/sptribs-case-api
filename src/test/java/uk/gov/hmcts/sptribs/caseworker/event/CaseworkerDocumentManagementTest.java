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
import uk.gov.hmcts.sptribs.caseworker.event.page.UploadCaseDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT;

@ExtendWith(MockitoExtension.class)
public class CaseworkerDocumentManagementTest {

    @InjectMocks
    private CaseworkerDocumentManagement caseworkerDocumentManagement;

    @InjectMocks
    private UploadCaseDocuments uploadCaseDocuments;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerDocumentManagement.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullyCheckUploadedDocumentsInMidEvent() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocumentUpload(getCaseworkerCICDocumentUploadList("file.pdf"))
            .build();
        caseData.setNewDocManagement(documentManagement);
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            uploadCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).isEmpty();
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
            caseworkerDocumentManagement.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getNewDocManagement().getCaseworkerCICDocument()).isEmpty();
        assertThat(response.getData().getNewDocManagement().getCaseworkerCICDocumentUpload()).isEmpty();
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument()).hasSize(1);
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocumentUpload()).isEmpty();
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue().getDocumentCategory())
            .isEqualTo(DocumentType.LINKED_DOCS);
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue().getDocumentEmailContent())
            .isEqualTo("some email content");
        assertThat(response.getData().getAllDocManagement()
            .getCaseworkerCICDocument().getFirst().getValue().getDocumentLink().getFilename())
            .isEqualTo("file.pdf");
        assertThat(response.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue().getDate()).isNotNull();
    }

    @Test
    void shouldSuccessfullySubmit() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        SubmittedCallbackResponse response = caseworkerDocumentManagement.submitted(updatedCaseDetails, beforeDetails);
        assertThat(response.getConfirmationHeader()).isEqualTo("# Case Updated");
    }

    @Test
    void shouldAddDocumentsToFurtherUploadedDocumentsWhenNewBundleOrderEnabled() {
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.YES);
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
            caseworkerDocumentManagement.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getFurtherUploadedDocuments()).hasSize(1);
        assertThat(response.getData().getFurtherUploadedDocuments().getFirst().getValue().getDocumentLink().getFilename())
            .isEqualTo("file.pdf");
    }

    @Test
    void shouldAddDocumentsToExistingFurtherUploadedDocumentsWhenNewBundleOrderEnabled() {
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.YES);
        // Pre-populate with existing documents
        caseData.setFurtherUploadedDocuments(getCaseworkerCICDocumentList("existing.pdf"));

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocumentUpload(getCaseworkerCICDocumentUploadList("new-file.pdf"))
            .build();
        caseData.setNewDocManagement(documentManagement);
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagement.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getFurtherUploadedDocuments()).hasSize(2);
        assertThat(response.getData().getFurtherUploadedDocuments().get(0).getValue().getDocumentLink().getFilename())
            .isEqualTo("existing.pdf");
        assertThat(response.getData().getFurtherUploadedDocuments().get(1).getValue().getDocumentLink().getFilename())
            .isEqualTo("new-file.pdf");
    }

    @Test
    void shouldNotAddDocumentsToFurtherUploadedDocumentsWhenNewBundleOrderDisabled() {
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.NO);
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
            caseworkerDocumentManagement.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getFurtherUploadedDocuments()).isNull();
    }
}
