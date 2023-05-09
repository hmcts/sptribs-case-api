package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithFileFormat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
public class HearingRecordingUploadPageTest {

    @InjectMocks
    private HearingRecordingUploadPage hearingRecordingUploadPage;

    @Test
    void shouldValidateUploadedDocument() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Listing listing = getRecordListing();
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("xml");
        HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors().contains(DOCUMENT_VALIDATION_MESSAGE)).isTrue();
    }
}
