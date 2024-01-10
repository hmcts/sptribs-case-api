package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IssueCaseSelectDocumentTest {

    @InjectMocks
    private IssueCaseSelectDocument issueCaseSelectDocument;
    
    @Mock
    private List<DynamicListElement> documentList;
    
    @Mock
    private DynamicMultiSelectList dynamicMultiSelectList;
    
    @Test
    void midEventReturnsErrorWhenMaxDocumentsExceeded() {
        when(dynamicMultiSelectList.getValue()).thenReturn(documentList);
        when(documentList.size()).thenReturn(6);
        
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseIssue caseIssue = CaseIssue.builder().documentList(dynamicMultiSelectList).build();
        CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);
        
        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains("Select up to 5 documents");
    }
    
    @Test
    void midEventReturnsErrorWhenNoDocumentsSelected() {
        when(dynamicMultiSelectList.getValue()).thenReturn(Collections.emptyList());
        
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseIssue caseIssue = CaseIssue.builder().documentList(dynamicMultiSelectList).build();
        CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);
        
        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains("Select at least one document");
    }
    
    @Test
    void midEventSuccessful() {
        when(dynamicMultiSelectList.getValue()).thenReturn(documentList);
        when(documentList.size()).thenReturn(3);
        
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseIssue caseIssue = CaseIssue.builder().documentList(dynamicMultiSelectList).build();
        CaseData caseData = CaseData.builder()
            .caseIssue(caseIssue)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseSelectDocument.midEvent(caseDetails, caseDetails);
        
        assertTrue(response.getErrors().isEmpty());
        assertThat(response.getData().getCaseIssue().getDocumentList().equals(dynamicMultiSelectList));
    }
}
