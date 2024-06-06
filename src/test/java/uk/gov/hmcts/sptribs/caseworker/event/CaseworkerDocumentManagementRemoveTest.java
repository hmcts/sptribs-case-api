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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ShowCaseDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2DocumentCiC;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@ExtendWith(MockitoExtension.class)
public class CaseworkerDocumentManagementRemoveTest {

    @InjectMocks
    private CaseworkerDocumentManagementRemove caseworkerDocumentManagementRemove;

    @InjectMocks
    private ShowCaseDocuments showCaseDocuments;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDocumentManagementRemove.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE);
    }

    @Test
    void shouldSuccessfullyRemoveDocument() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> start =
            caseworkerDocumentManagementRemove.aboutToStart(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementRemove.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementRemove.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(start).isNotNull();
        assertThat(response).isNotNull();
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldRemoveDocumentSuccessfully() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build());
        caseData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build());

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setAllDocManagement(documentManagement);
        updatedCaseDetails.setData(caseData);
        final CaseData oldData = caseData();

        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        DocumentManagement documentManagementOld = DocumentManagement.builder().caseworkerCICDocument(get2Document()).build();
        oldData.setAllDocManagement(documentManagementOld);
        Order orderOld = Order.builder().uploadedFile(get2DocumentCiC()).build();
        Order orderOld2 = Order.builder().uploadedFile(get2DocumentCiC()).build();
        ListValue<Order> orderListValueOld = new ListValue<>();
        orderListValueOld.setValue(orderOld);
        ListValue<Order> orderListValueOld2 = new ListValue<>();
        orderListValueOld2.setValue(orderOld2);
        List<ListValue<Order>> orderList = new ArrayList<>();
        orderList.add(orderListValueOld);
        orderList.add(orderListValueOld2);
        CicCase cicCaseOld = CicCase.builder()
            .orderList(orderList)
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);
        oldData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(get2Document()).build()).build());
        oldData.setCloseCase(CloseCase.builder().documents(get2Document()).build());
        beforeDetails.setData(oldData);
        //When

        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            showCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementRemove.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementRemove.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(midResponse).isNotNull();
        assertThat(response).isNotNull();
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldRemoveDocumentSuccessfullyWithAboutToStart() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CICDocument doc = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        caseData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(doc).build());
        caseData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(doc).build());

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .removedDocumentList(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setAllDocManagement(documentManagement);
        updatedCaseDetails.setData(caseData);
        final CaseData oldData = caseData();

        CICDocument docOld = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        oldData.setCaseIssueFinalDecision(CaseIssueFinalDecision.builder().document(docOld).build());
        oldData.setCaseIssueDecision(CaseIssueDecision.builder().decisionDocument(docOld).build());

        DocumentManagement documentManagementOld = DocumentManagement.builder().caseworkerCICDocument(get2Document()).build();
        oldData.setAllDocManagement(documentManagementOld);
        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();
        oldData.setCicCase(cicCaseOld);
        oldData.setListing(Listing.builder().summary(HearingSummary.builder().recFile(get2Document()).build()).build());
        oldData.setCloseCase(CloseCase.builder().documents(get2Document()).build());
        beforeDetails.setData(oldData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> start =
            caseworkerDocumentManagementRemove.aboutToStart(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> midResponse =
            showCaseDocuments.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementRemove.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementRemove.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(start).isNotNull();
        assertThat(midResponse).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getRemovedDocumentList()).hasSize(0);
        assertThat(documentMgmtResponse).isNotNull();
    }


}
