package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.DocumentConstants;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerSelectedCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

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

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerDocumentManagementAmend.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(caseworkerDocumentManagementAmend, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerDocumentManagementAmend.configure(configBuilder);

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
        cicCase.getApplicantDocumentsUploaded().get(0).setValue(getCaseworkerCICDocument());
        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            selectCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(midResponse.getData().getCicCase().getSelectedDocumentToAmend()).isNotNull();
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentToAmend().getDocumentLink()).isNotNull();
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentToAmend().getDocumentCategory())
            .isEqualTo(APPLICATION_FORM);
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentToAmend().getDocumentEmailContent())
            .isEqualTo("updated email content");

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToStartResponse.getData().getCicCase().getAmendDocumentList().getListItems()).isNotEmpty();
        assertThat(midResponse).isNotNull();
        assertThat(midResponse.getData().getCicCase().getSelectedDocumentType()).isEqualTo(DocumentConstants.CASE_TYPE);
        assertThat(aboutToSubmitResponse.getData().getCicCase().getApplicantDocumentsUploaded().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCicCase()
            .getApplicantDocumentsUploaded().get(0)
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

        cicCase.setSelectedDocumentToAmend(getCaseworkerSelectedCICDocument());
        cicCase.setSelectedDocumentType(DocumentConstants.REINSTATE_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getCicCase().getReinstateDocuments().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCicCase().getReinstateDocuments().get(0)
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

        cicCase.setSelectedDocumentToAmend(getCaseworkerSelectedCICDocument());
        cicCase.setSelectedDocumentType(DocumentConstants.DOC_MGMT_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getAllDocManagement().getCaseworkerCICDocument().get(0)
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

        cicCase.setSelectedDocumentToAmend(getCaseworkerSelectedCICDocument());
        cicCase.setSelectedDocumentType(DocumentConstants.CLOSE_CASE_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getCloseCase().getDocuments().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getCloseCase().getDocuments().get(0)
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

        cicCase.setSelectedDocumentToAmend(getCaseworkerSelectedCICDocument());
        cicCase.setSelectedDocumentType(DocumentConstants.HEARING_SUMMARY_TYPE);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse =
            caseworkerDocumentManagementAmend.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementAmend.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(aboutToSubmitResponse.getData().getListing().getSummary().getRecFile().get(0).getValue()).isNotNull();
        assertThat(aboutToSubmitResponse.getData().getListing().getSummary().getRecFile().get(0)
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

    private CaseworkerSelectedCICDocument getCaseworkerSelectedCICDocument() {
        return CaseworkerSelectedCICDocument.builder()
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

}
