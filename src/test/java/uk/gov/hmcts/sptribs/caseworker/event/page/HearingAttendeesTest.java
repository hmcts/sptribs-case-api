package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class HearingAttendeesTest {
    @InjectMocks
    private HearingAttendees hearingAttendees;

    @Test
    void shouldPopulateJudgeWithPreviouslySelectedData() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0")
            .code(UUID.randomUUID())
            .build();
        final DynamicList judgeList = DynamicList
            .builder()
            .listItems(List.of(listItem))
            .build();
        caseData.setListing(
            Listing.builder()
                .summary(HearingSummary.builder()
                    .judge(judgeList)
                    .build()
                )
                .build()
        );
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();
        final CaseData caseDataBefore = caseData();
        final DynamicList judgeListBefore = DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
        caseDataBefore.setListing(
            Listing.builder()
                .summary(HearingSummary.builder()
                    .judge(judgeListBefore)
                    .build()
                )
                .build()
        );
        caseDetailsBefore.setData(caseDataBefore);

        // When
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingAttendees.midEvent(caseDetails, caseDetailsBefore);

        // Then
        assertThat(response.getData().getListing().getSummary().getJudge().getValue()).isEqualTo(listItem);
    }

    @Test
    void shouldNotSelectJudgeWithNoPreviouslySelectedData() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("0")
            .code(UUID.randomUUID())
            .build();
        final DynamicList judgeList = DynamicList
            .builder()
            .listItems(List.of(listItem))
            .build();
        caseData.setListing(
            Listing.builder()
                .summary(HearingSummary.builder()
                    .judge(judgeList)
                    .build()
                )
                .build()
        );
        caseDetails.setData(caseData);

        // When
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingAttendees.midEvent(caseDetails, caseDetails);

        // Then
        assertThat(response.getData().getListing().getSummary().getJudge().getValue()).isNull();
    }
}
