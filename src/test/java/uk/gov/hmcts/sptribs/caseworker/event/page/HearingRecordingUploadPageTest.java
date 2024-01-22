package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
public class HearingRecordingUploadPageTest {

    @InjectMocks
    private HearingRecordingUploadPage hearingRecordingUploadPage;
    private CaseDetails<CaseData, State> caseDetails;
    private Listing listing;
    private List<ListValue<CaseworkerCICDocument>> documentList;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails<>();
        listing = getRecordListing();
    }

    @Test
    void midEventReturnsNoErrors() {
        documentList = getCaseworkerCICDocumentList("file.mp3");
        final HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(0);
    }

    @Test
    void midEventReturnsErrorForInvalidDocumentType() {
        //try to upload .xml (non-supported)
        documentList = getCaseworkerCICDocumentList("file.xml");
        final HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Please attach a mp3 document");
    }

    @Test
    void midEventReturnsErrorForMissingDescription() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("file.mp3").build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .build();
        final ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Description is mandatory for each document");
    }

    @Test
    void midEventReturnsErrorForMissingDocumentLink() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        final ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Please attach the document");
    }

    @Test
    void midEventReturnsErrorForMissingCategory() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("file.mp3").build())
            .documentEmailContent("some email content")
            .build();
        final ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Category is mandatory for each document");
    }
}
