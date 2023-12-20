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
import uk.gov.hmcts.sptribs.common.event.page.CaseUploadDocuments;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;

@ExtendWith(MockitoExtension.class)
public class CaseUploadDocumentsTest {

    @InjectMocks
    private CaseUploadDocuments caseUploadDocuments;

    @Test
    void shouldValidateUploadedDocument() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .applicantDocumentsUploaded(getCaseworkerCICDocumentList())
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = caseUploadDocuments.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotNull();
    }
}
