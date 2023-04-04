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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.service.DocumentListService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@ExtendWith(MockitoExtension.class)
public class CaseworkerDocumentManagementRemoveTest {

    @InjectMocks
    private CaseworkerDocumentManagementRemove caseworkerDocumentManagementRemove;

    @Mock
    DocumentListService documentListService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseworkerDocumentManagementRemove.setCaseFileViewAndDocumentManagementEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDocumentManagementRemove.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE);
    }

    @Test
    void shouldNotConfigureDocumentManagementRemoveIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerDocumentManagementRemove.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE);
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
        when(documentListService.getAllDecisionDocuments(any())).thenReturn(getDocument());
        when(documentListService.getAllFinalDecisionDocuments(any())).thenReturn(getDocument());
        when(documentListService.getAllOrderDocuments(any())).thenReturn(getDocument());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> start =
            caseworkerDocumentManagementRemove.aboutToStart(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementRemove.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementRemove.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(documentMgmtResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyRemoveDocumentSuccessfully() {
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
        beforeDetails.setData(caseData);

        updatedCaseDetails.setState(State.CaseManagement);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(documentListService.getAllDecisionDocuments(any())).thenReturn(get2Document());
        when(documentListService.getAllFinalDecisionDocuments(any())).thenReturn(get2Document());
        when(documentListService.getAllOrderDocuments(any())).thenReturn(get2Document());
        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(getDocument())
            .finalDecisionDocumentList(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        //When

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerDocumentManagementRemove.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse documentMgmtResponse = caseworkerDocumentManagementRemove.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(documentMgmtResponse).isNotNull();
    }

    private List<ListValue<CaseworkerCICDocument>> getDocument() {
        List<ListValue<CaseworkerCICDocument>> listValueList = get2Document();
        ListValue<CaseworkerCICDocument> last = listValueList.get(1);
        listValueList.remove(last);
        return listValueList;
    }

    private List<ListValue<CaseworkerCICDocument>> get2Document() {
        List<ListValue<CaseworkerCICDocument>> listValueList = new ArrayList<>();
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
            .build();
        ListValue<CaseworkerCICDocument> list = new ListValue<>();
        list.setValue(doc);
        listValueList.add(list);
        CaseworkerCICDocument doc2 = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("url").filename("name").build())
            .build();
        ListValue<CaseworkerCICDocument> list2 = new ListValue<>();
        list2.setValue(doc2);
        listValueList.add(list2);
        return listValueList;
    }
}
