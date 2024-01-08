package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.DocumentUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class ReinstateUploadDocumentsTest {

    @InjectMocks
    private ReinstateUploadDocuments reinstateUploadDocuments;
    
    @Test
    void midEventReturnsNoErrorsWithUploadedDocuments() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(getCaseworkerCICDocumentList("file.pdf"))
            .build();
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response = reinstateUploadDocuments.midEvent(caseDetails, caseDetails);
        assertThat(response.getData().getCicCase().getReinstateDocuments()).isNotNull();
        assertTrue(response.getErrors().isEmpty());
    }
    
    @Test
    void midEventReturnsErrorWithWrongDocumentType() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
        .reinstateDocuments(getCaseworkerCICDocumentList("file.xml"))
        .build();
        CaseData caseData = CaseData.builder()
        .cicCase(cicCase)
        .build();
        caseDetails.setData(caseData);
        final AboutToStartOrSubmitResponse<CaseData, State> response = reinstateUploadDocuments.midEvent(caseDetails, caseDetails);
        
        assertTrue(response.getErrors().contains(DOCUMENT_VALIDATION_MESSAGE));
    }
    
    @Test
    void midEventValidatesUploadedDocuments() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(getCaseworkerCICDocumentList("file.xml"))
            .build();
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);
        try (MockedStatic<DocumentUtil> mockedDocumentUtils = Mockito.mockStatic(DocumentUtil.class)) {
            mockedDocumentUtils.when(() -> DocumentUtil.validateUploadedDocuments(anyList()))
                .thenReturn(Collections.emptyList());
            
            reinstateUploadDocuments.midEvent(caseDetails, caseDetails);
        
            mockedDocumentUtils.verify(() ->  DocumentUtil.validateUploadedDocuments(anyList()), times(1));
        }
    }
}
