package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(MockitoExtension.class)
public class EditHearingSummarySelectTest {

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private EditHearingSummarySelect editHearingSummarySelect;

    @Test
    void midEventShouldPopulateRecFileUploadAfterHearingChosenIfHearingMatches() {
        final ListValue<Listing> listingListValue = new ListValue<>();
        final Listing listing = getRecordListing();
        listing.setSummary(HearingSummary.builder().recFile(getCaseworkerCICDocumentList("file.mp3")).build());
        listingListValue.setValue(listing);
        final List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(listingListValue);

        final CicCase cicCase = CicCase.builder()
            .hearingSummaryList(
                DynamicList.builder()
                    .value(
                        DynamicListElement.builder()
                            .label("1 - Final - 21 Apr 2021 10:00")
                            .build()
                    ).build()
            ).build();

        final CaseData caseData = CaseData.builder()
            .hearingList(listValueList)
            .cicCase(cicCase)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);

        when(hearingService.isMatchingHearing(eq(listingListValue), eq("1 - Final - 21 Apr 2021 10:00")))
            .thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            editHearingSummarySelect.midEvent(updatedCaseDetails, updatedCaseDetails);

        assertThat(response.getData().getListing().getSummary().getRecFileUpload()).hasSize(1);
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentCategory())
            .isEqualTo(DocumentType.LINKED_DOCS);
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentEmailContent())
            .isEqualTo("some email content");
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentLink().getFilename())
            .isEqualTo("file.mp3");
    }

    @Test
    void midEventShouldNotPopulateRecFileUploadAfterHearingChosenIfNoHearingMatches() {
        final ListValue<Listing> listingListValue = new ListValue<>();
        final Listing listing = getRecordListing();
        listing.setSummary(HearingSummary.builder().recFile(getCaseworkerCICDocumentList("file.mp3")).build());
        listingListValue.setValue(listing);
        final List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(listingListValue);

        final CicCase cicCase = CicCase.builder()
            .hearingSummaryList(
                DynamicList.builder()
                    .value(
                        DynamicListElement.builder()
                            .label("1 - Final - 21 Apr 2021 10:10")
                            .build()
                    ).build()
            ).build();

        final CaseData caseData = CaseData.builder()
            .hearingList(listValueList)
            .cicCase(cicCase)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);

        when(hearingService.isMatchingHearing(eq(listingListValue), eq("1 - Final - 21 Apr 2021 10:10")))
            .thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            editHearingSummarySelect.midEvent(updatedCaseDetails, updatedCaseDetails);

        assertThat(response.getData().getListing().getSummary().getRecFileUpload()).isNull();
    }
}
