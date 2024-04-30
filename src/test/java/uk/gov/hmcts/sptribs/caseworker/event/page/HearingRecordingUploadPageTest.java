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
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
public class HearingRecordingUploadPageTest {

    public static final String ERROR_ATTACH_THE_DOCUMENT = "Please attach the document";
    public static final String PLEASE_ATTACH_A_MP_3_DOCUMENT = "Please attach a mp3 document";
    public static final String ERROR_ATTACH_MP3 = PLEASE_ATTACH_A_MP_3_DOCUMENT;
    public static final String ERROR_DESCRIPTION_IS_MANDATORY = "Description is mandatory for each document";
    public static final String ERROR_CATEGORY_IS_MANDATORY = "Category is mandatory for each document";

    @InjectMocks
    private HearingRecordingUploadPage hearingRecordingUploadPage;
    private CaseDetails<CaseData, State> caseDetails;
    private Listing listing;
    private List<ListValue<CaseworkerCICDocumentUpload>> documentList;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails<>();
        listing = getRecordListing();
    }

    @Test
    void midEventReturnsNoErrorsWithDocumentList() {
        documentList = getCaseworkerCICDocumentUploadList("file.mp3");
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsNoErrorForEmptyDocumentList() {
        final HearingSummary hearingSummary = HearingSummary.builder().build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsErrorForInvalidDocumentType() {
        //try to upload .xml (non-supported)
        documentList = getCaseworkerCICDocumentUploadList("file.xml");
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains(ERROR_ATTACH_MP3);
    }

    @Test
    void midEventReturnsErrorForMissingDescription() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().filename("file.mp3").build())
            .documentCategory(DocumentType.LINKED_DOCS)
            .build();
        final ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains(ERROR_DESCRIPTION_IS_MANDATORY);
    }

    @Test
    void midEventReturnsErrorForMissingDocumentLink() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        final ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains(ERROR_ATTACH_THE_DOCUMENT);
    }

    @Test
    void midEventReturnsErrorForMissingCategory() {
        documentList = new ArrayList<>();
        final CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().filename("file.mp3").build())
            .documentEmailContent("some email content")
            .build();
        final ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        documentList.add(documentListValue);
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains(ERROR_CATEGORY_IS_MANDATORY);
    }

    @Test
    void midEventReturnsCorrectlyForMixedDocumentList() {
        documentList = getMixedCaseworkerCicDocumentList();
        final HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        listing.setSummary(hearingSummary);

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingRecordingUploadPage.midEvent(caseDetails, caseDetails);
        final long numberOfMissingDocuments =
            response.getErrors().stream().filter(error -> error.contains(ERROR_ATTACH_THE_DOCUMENT)).count();

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).hasSize(5);
        assertThat(response.getErrors()).contains(ERROR_ATTACH_THE_DOCUMENT);
        assertThat(numberOfMissingDocuments).isEqualTo(2);
        assertThat(response.getErrors()).contains(ERROR_ATTACH_MP3);
        assertThat(response.getErrors()).contains(ERROR_DESCRIPTION_IS_MANDATORY);
        assertThat(response.getErrors()).contains(ERROR_CATEGORY_IS_MANDATORY);
    }

    private static List<ListValue<CaseworkerCICDocumentUpload>> getMixedCaseworkerCicDocumentList() {
        final List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        final CaseworkerCICDocumentUpload validDocument = CaseworkerCICDocumentUpload.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("Some description email")
            .documentLink(Document.builder().filename("file.mp3").build())
            .build();
        final CaseworkerCICDocumentUpload emptyDocument = CaseworkerCICDocumentUpload.builder()
            .build();
        final CaseworkerCICDocumentUpload nullDocument = CaseworkerCICDocumentUpload.builder()
            .documentLink(Document.builder().filename(null).build())
            .build();
        final ListValue<CaseworkerCICDocumentUpload> listValue1 = new ListValue<>();
        listValue1.setValue(validDocument);
        final ListValue<CaseworkerCICDocumentUpload> listValue2 = new ListValue<>();
        listValue2.setValue(emptyDocument);
        final ListValue<CaseworkerCICDocumentUpload> listValue3 = new ListValue<>();
        listValue3.setValue(nullDocument);
        final ListValue<CaseworkerCICDocumentUpload> listValue4 = new ListValue<>();
        listValue4.setValue(null);
        documentList.add(listValue1);
        documentList.add(listValue2);
        documentList.add(listValue3);
        documentList.add(listValue4);
        return documentList;
    }
}
