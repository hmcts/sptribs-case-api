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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;

@ExtendWith(MockitoExtension.class)
public class SendOrderUploadOrderTest {

    @InjectMocks
    private SendOrderUploadOrder sendOrderUploadOrder;

    @Test
    void midEventReturnsErrorWithWrongDocumentType() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .orderFile(getCICDocumentList("file.uml"))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = sendOrderUploadOrder.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertTrue(response.getErrors().contains(DOCUMENT_VALIDATION_MESSAGE));
    }

    @Test
    void midEventReturnsNoErrorsWithUploadedDocuments() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .orderFile(getCICDocumentList("file.pdf"))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = sendOrderUploadOrder.midEvent(caseDetails, caseDetails);

        assertFalse(response.getData().getCicCase().getOrderFile().isEmpty());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void midEventValidatesUploadedDocuments() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .orderFile(getCICDocumentList("file.txt"))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);
        try (MockedStatic<DocumentUtil> mockedDocumentUtils = Mockito.mockStatic(DocumentUtil.class)) {
            mockedDocumentUtils.when(() -> DocumentUtil.validateUploadedDocuments(anyList()))
                .thenReturn(Collections.emptyList());

            sendOrderUploadOrder.midEvent(caseDetails, caseDetails);

            mockedDocumentUtils.verify(() ->  DocumentUtil.validateDocumentFormat(anyList()), times(1));
        }
    }
}
