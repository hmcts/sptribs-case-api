package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementAmendDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementSelectDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_AMEND;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.APPLICATION_FORM;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;

@ExtendWith(MockitoExtension.class)
class CaseworkerDocumentManagementAmendTest {

    @InjectMocks
    private CaseworkerDocumentManagementAmend caseworkerDocumentManagementAmend;

    @InjectMocks
    private DocumentManagementSelectDocuments selectCaseDocuments;

    @InjectMocks
    private DocumentManagementAmendDocuments amendCaseDocuments;

    private static final String UPDATED_EMAIL_CONTENT = "updated email content";

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerDocumentManagementAmend.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND);

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
    void shouldSuccessfullyAmendCaseDocument() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(getCaseworkerCICDocumentList("test.pdf", APPLICATION_FORM))
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse =
            caseworkerDocumentManagementAmend.aboutToStart(updatedCaseDetails);

        cicCase.getAmendDocumentList().setValue(getDynamicListItems());
        cicCase.getApplicantDocumentsUploaded().getFirst().setValue(getCaseworkerCICDocument());
        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            selectCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(midResponse.getData().getCicCase().getSelectedDocumentLink()).isNotNull();
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentEmailContent()).isEqualTo("updated email content");

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToStartResponse.getData().getCicCase().getAmendDocumentList().getListItems()).isNotEmpty();
        assertThat(midResponse).isNotNull();
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentType()).isEqualTo(DocumentConstants.CASE_TYPE);
        assertThat(aboutToSubmitResponse.getData().getCicCase().getApplicantDocumentsUploaded().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCicCase()
            .getApplicantDocumentsUploaded().getFirst()
            .getValue().getDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyAmendReinstateCaseDocument() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .reinstateDocuments(getCaseworkerCICDocumentList())
            .build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(getDocumentData());
        cicCase.setSelectedDocumentType(DocumentConstants.REINSTATE_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getCicCase().getReinstateDocuments().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCicCase().getReinstateDocuments().getFirst()
            .getValue().getDocumentCategory())
            .isEqualTo(APPLICATION_FORM);
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyAmendDocumentMgmtFiles() {
        //Given
        final CaseData caseData = caseData();
        caseData.getAllDocManagement().setCaseworkerCICDocument(getCaseworkerCICDocumentList());
        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(getDocumentData());
        cicCase.setSelectedDocumentType(DocumentConstants.DOC_MGMT_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst()
            .getValue().getDocumentCategory())
            .isEqualTo(APPLICATION_FORM);
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyAmendCloseCaseDocuments() {
        //Given
        final CaseData caseData = caseData();
        caseData.getCloseCase().setDocuments(getCaseworkerCICDocumentList());
        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(getDocumentData());
        cicCase.setSelectedDocumentType(DocumentConstants.CLOSE_CASE_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getCloseCase().getDocuments().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCloseCase().getDocuments().getFirst()
            .getValue().getDocumentCategory())
            .isEqualTo(APPLICATION_FORM);
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyAmendHearingSummaryDocuments() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().getSummary().setRecFile(getCaseworkerCICDocumentList("file.pdf"));
        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(getDocumentData());
        cicCase.setSelectedDocumentType(DocumentConstants.HEARING_SUMMARY_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getListing().getSummary().getRecFile().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getListing().getSummary().getRecFile().getFirst()
            .getValue().getDocumentCategory())
            .isEqualTo(DocumentType.LINKED_DOCS);
        assertThat(documentMgmtResponse).isNotNull();
    }

    private Document getDocumentData() {
        return Document.builder()
            .filename("test.pdf")
            .binaryUrl("http://url/")
            .url("http://url/")
            .build();
    }

    private CICDocument getCicDocumentData() {
        return CICDocument.builder()
            .documentEmailContent("email content")
            .documentLink(getDocumentData())
            .build();
    }

    private List<ListValue<Order>> getOrderTemplateDocumentList() {
        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(getDocumentData())
            .build();
        Order order = Order.builder().draftOrder(draftOrderCIC).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        return List.of(orderListValue);
    }

    private List<ListValue<Order>> getOrderList() {
        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        return List.of(orderListValue);
    }

    private CaseworkerCICDocument getCaseworkerCICDocument() {
        return CaseworkerCICDocument.builder()
            .documentLink(getDocumentData())
            .documentCategory(APPLICATION_FORM)
            .documentEmailContent("updated email content")
            .build();
    }

    private DynamicListElement getDynamicListItems() {
        return DynamicListElement
            .builder()
            .label("CASE--test.pdf--A - Application Form")
            .code(UUID.randomUUID())
            .build();
    }

    private List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentListWithUrl(String fileName, String url) {
        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(Document.builder()
                .filename(fileName)
                .binaryUrl(url)
                .url(url)
                .build())
            .documentCategory(APPLICATION_FORM)
            .documentEmailContent("some email content")
            .build();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);
        return documentList;
    }

    @Test
    void shouldUpdateInitialCicaDocumentsAndFurtherUploadedDocumentsWhenNewBundleOrderEnabled() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.YES);

        // Create documents with matching URLs for the update to work
        final Document testDocument = getDocumentData();
        final List<ListValue<CaseworkerCICDocument>> initialDocs = getCaseworkerCICDocumentListWithUrl("initial.pdf",
            testDocument.getUrl());
        final List<ListValue<CaseworkerCICDocument>> furtherDocs = getCaseworkerCICDocumentListWithUrl("further.pdf",
            testDocument.getUrl());
        final List<ListValue<CaseworkerCICDocument>> docMgmtDocs = getCaseworkerCICDocumentListWithUrl("doc-mgmt.pdf",
            testDocument.getUrl());

        caseData.setInitialCicaDocuments(initialDocs);
        caseData.setFurtherUploadedDocuments(furtherDocs);
        caseData.getAllDocManagement().setCaseworkerCICDocument(docMgmtDocs);

        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(testDocument);
        cicCase.setSelectedDocumentType(DocumentConstants.DOC_MGMT_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        // Should update the main document management list
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst()
            .getValue().getDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst()
            .getValue().getDocumentEmailContent()).isEqualTo(UPDATED_EMAIL_CONTENT);

        // Should also update initial CICA documents when new bundle order is enabled
        assertThat(aboutToSubmitResponse.getData().getInitialCicaDocuments().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getInitialCicaDocuments().getFirst()
            .getValue().getDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(aboutToSubmitResponse.getData().getInitialCicaDocuments().getFirst()
            .getValue().getDocumentEmailContent()).isEqualTo(UPDATED_EMAIL_CONTENT);

        // Should also update further uploaded documents when new bundle order is enabled
        assertThat(aboutToSubmitResponse.getData().getFurtherUploadedDocuments().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getFurtherUploadedDocuments().get(0)
            .getValue().getDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(aboutToSubmitResponse.getData().getFurtherUploadedDocuments().get(0)
            .getValue().getDocumentEmailContent()).isEqualTo(UPDATED_EMAIL_CONTENT);
    }

    @Test
    void shouldNotUpdateInitialCicaDocumentsAndFurtherUploadedDocumentsWhenNewBundleOrderDisabled() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNewBundleOrderEnabled(YesNo.NO);

        // Create documents with matching URLs for the update to work
        final Document testDocument = getDocumentData();
        final List<ListValue<CaseworkerCICDocument>> initialDocs = getCaseworkerCICDocumentListWithUrl("initial.pdf",
            testDocument.getUrl());
        final List<ListValue<CaseworkerCICDocument>> furtherDocs = getCaseworkerCICDocumentListWithUrl("further.pdf",
            testDocument.getUrl());
        final List<ListValue<CaseworkerCICDocument>> docMgmtDocs = getCaseworkerCICDocumentListWithUrl("doc-mgmt.pdf",
            testDocument.getUrl());

        caseData.setInitialCicaDocuments(initialDocs);
        caseData.setFurtherUploadedDocuments(furtherDocs);
        caseData.getAllDocManagement().setCaseworkerCICDocument(docMgmtDocs);

        final CicCase cicCase = CicCase.builder().build();
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        cicCase.setSelectedDocumentCategory(APPLICATION_FORM);
        cicCase.setSelectedDocumentEmailContent(UPDATED_EMAIL_CONTENT);
        cicCase.setSelectedDocumentLink(testDocument);
        cicCase.setSelectedDocumentType(DocumentConstants.DOC_MGMT_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        // Should update the main document management list
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst().getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst()
            .getValue().getDocumentCategory()).isEqualTo(APPLICATION_FORM);
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().getFirst()
            .getValue().getDocumentEmailContent()).isEqualTo(UPDATED_EMAIL_CONTENT);

        // Should NOT update initial CICA documents when new bundle order is disabled
        assertThat(aboutToSubmitResponse.getData().getInitialCicaDocuments().getFirst()
            .getValue().getDocumentEmailContent()).isEqualTo("some email content"); // Original value

        // Should NOT update further uploaded documents when new bundle order is disabled
        assertThat(aboutToSubmitResponse.getData().getFurtherUploadedDocuments().getFirst()
            .getValue().getDocumentEmailContent()).isEqualTo("some email content"); // Original value
    }

}
