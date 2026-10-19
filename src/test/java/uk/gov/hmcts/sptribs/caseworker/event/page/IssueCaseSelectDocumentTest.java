package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.ISSUE_CASE_NOTIFICATION_FAILED;

@ExtendWith(MockitoExtension.class)
class IssueCaseSelectDocumentTest {

    @InjectMocks
    private IssueCaseSelectDocument issueCaseSelectDocument;

    @Mock
    private List<DynamicListElement> documentList;

    @Mock
    private DynamicMultiSelectList dynamicMultiSelectList;

    @Test
    void midEventSuccessful() {
        UUID documentId = UUID.randomUUID();
        Document document = Document.builder()
            .url("https://example.test/documents/" + documentId)
            .binaryUrl("https://example.test/documents/" + documentId + "/binary")
            .filename("selected.pdf")
            .build();
        CaseworkerCICDocument selectedDocument = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentLink(document)
            .build();
        ListValue<CaseworkerCICDocument> documentValue = new ListValue<>();
        documentValue.setValue(selectedDocument);
        DynamicListElement selection = DynamicListElement.builder()
            .code(documentId)
            .label("[selected.pdf](https://example.test/documents/" + documentId + "/binary)")
            .build();
        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .value(List.of(selection))
            .listItems(List.of(selection))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().applicantDocumentsUploaded(List.of(documentValue)).build())
            .caseIssue(CaseIssue.builder().documentList(list).build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);

        assertTrue(response.getErrors().isEmpty());
        assertThat(response.getData().getCaseIssue().getDocumentList()).isEqualTo(list);
        assertThat(response.getData().getCaseIssue().getD01()).isEqualTo(selectedDocument);
    }

    @Test
    void midEventRejectsMissingCaseDetails() {
        DynamicListElement selection = DynamicListElement.builder().code(UUID.randomUUID()).build();
        CaseData data = CaseData.builder()
            .cicCase(null)
            .caseIssue(CaseIssue.builder()
                .documentList(DynamicMultiSelectList.builder().value(List.of(selection)).listItems(List.of(selection)).build())
                .build())
            .build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(ISSUE_CASE_NOTIFICATION_FAILED);
        assertThat(data.getCaseIssue().getD01()).isNull();
    }

    @Test
    void midEventRejectsAnUnknownSelectedOption() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder()
                .url("https://example.test/documents/" + UUID.randomUUID()).filename("document.pdf").build())
            .build();
        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicListElement.builder().code(UUID.randomUUID()).build()))
            .value(List.of(DynamicListElement.builder().code(UUID.randomUUID()).build()))
            .build();
        CaseData data = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(List.of(new ListValue<>("1", document))).build())
            .caseIssue(CaseIssue.builder().documentList(list).build())
            .build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(data);

        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(ISSUE_CASE_NOTIFICATION_FAILED);
        assertThat(data.getCaseIssue().getD01()).isNull();
    }

    @Test
    void midEventReturnsErrorWhenMaxDocumentsExceeded() {
        when(dynamicMultiSelectList.getValue()).thenReturn(documentList);
        when(documentList.size()).thenReturn(6);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssue caseIssue = CaseIssue.builder().documentList(dynamicMultiSelectList).build();
        final CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);

        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains("Select up to 5 documents");
    }

    @Test
    void midEventReturnsErrorForNullDocumentList() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssue caseIssue = CaseIssue.builder().build();
        final CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Select at least one document");
    }

    @Test
    void midEventReturnsErrorWhenNoDocumentsSelected() {
        when(dynamicMultiSelectList.getValue()).thenReturn(Collections.emptyList());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssue caseIssue = CaseIssue.builder().documentList(dynamicMultiSelectList).build();
        final CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);

        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains("Select at least one document");
    }
}
