package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithFileFormat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
public class HearingRecordingUploadPageTest {

    @InjectMocks
    private HearingRecordingUploadPage hearingRecordingUploadPage;

    @Test
    void shouldValidateUploadedDocument() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Listing listing = getRecordListing();
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("xml");
        HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldValidateUploadedDocumentWithoutCategoryWithoutDesc() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Listing listing = getRecordListing();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("file.mp3").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(2);
    }

    @Test
    void shouldValidateUploadedDocumentWithoutDoc() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Listing listing = getRecordListing();
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }
}
