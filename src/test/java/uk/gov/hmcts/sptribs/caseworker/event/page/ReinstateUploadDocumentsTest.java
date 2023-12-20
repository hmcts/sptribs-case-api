package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithFileFormat;

@ExtendWith(MockitoExtension.class)
public class ReinstateUploadDocumentsTest {

    @InjectMocks
    private ReinstateUploadDocuments reinstateUploadDocuments;

    @Test
    void shouldValidateUploadedDocument() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .reinstateDocuments(getCaseworkerCICDocumentListWithFileFormat("xml"))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = reinstateUploadDocuments.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors().contains(DOCUMENT_VALIDATION_MESSAGE)).isTrue();
    }
}
