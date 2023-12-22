package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.sptribs.common.event.page.CaseUploadDocuments;
import uk.gov.hmcts.sptribs.document.DocumentUtil;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;

@ExtendWith(MockitoExtension.class)
public class CaseUploadDocumentsTest {

    @InjectMocks
    private CaseUploadDocuments caseUploadDocuments;

    private CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {
        this.caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(getCaseworkerCICDocumentList("file.pdf"))
            .build();
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        this.caseDetails.setData(caseData);
    }

    @Test
    void midEventReturnsNoErrorsWithUploadedDocuments() {
        AboutToStartOrSubmitResponse<CaseData, State> response = caseUploadDocuments.midEvent(this.caseDetails, this.caseDetails);
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotNull();
        assertTrue(response.getErrors().isEmpty());
    }
    
    @Test
    void midEventValidatesUploadedDocuments() {
        try (MockedStatic<DocumentUtil> mockedDocumentUtils = Mockito.mockStatic(DocumentUtil.class)) {
            mockedDocumentUtils.when(() -> DocumentUtil.validateUploadedDocuments(anyList()))
                .thenReturn(Collections.emptyList());
            
            caseUploadDocuments.midEvent(this.caseDetails, this.caseDetails);
        
            mockedDocumentUtils.verify(() ->  DocumentUtil.validateUploadedDocuments(anyList()), times(1));
        }
    }
}
