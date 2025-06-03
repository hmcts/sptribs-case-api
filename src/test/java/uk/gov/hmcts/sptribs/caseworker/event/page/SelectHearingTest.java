package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;


@ExtendWith(MockitoExtension.class)
class SelectHearingTest {

    @Mock
    private CicCase cicCase;

    @Mock
    private DynamicList cicCaseHearingList;

    @Mock
    private DynamicListElement cicCaseHearingLabel;

    @InjectMocks
    private SelectHearing selectHearing;

    private Listing initialListing;

    private CaseDetails<CaseData, State> caseDetails;
    private ListValue<Listing> finalListingListValue;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails<>();
        finalListingListValue = new ListValue<>();
        initialListing = new Listing();

        initialListing.setHearingFormat(HearingFormat.TELEPHONE);
        initialListing.setConferenceCallNumber("");
        initialListing.setHearingType(HearingType.CASE_MANAGEMENT);
        initialListing.setImportantInfoDetails("initial details");
        initialListing.setDate(LocalDate.now());
        initialListing.setHearingTime("9:30");
        initialListing.setDate(LocalDate.of(2023, 3, 15));
        initialListing.setHearingStatus(HearingState.Listed);

        finalListingListValue.setValue(getRecordListing());
        finalListingListValue.setId("Final 10:00");

        List<ListValue<Listing>> listValueList = new ArrayList<>();
        listValueList.add(finalListingListValue);

        CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        caseData.setListing(initialListing);
        caseData.setHearingList(listValueList);
        caseDetails.setData(caseData);
    }

    @Test
    void shouldReturnHearingSelected() {
        when(cicCaseHearingLabel.getLabel()).thenReturn("Final 10:00");
        when(cicCaseHearingList.getValue()).thenReturn(cicCaseHearingLabel);
        when(cicCase.getHearingList()).thenReturn(cicCaseHearingList);

        final AboutToStartOrSubmitResponse<CaseData, State> response = selectHearing.midEvent(caseDetails, caseDetails);

        assertThat(response.getData().getListing()).isEqualTo(finalListingListValue.getValue());
        assertThat(response.getData().getListing()).isNotEqualTo(initialListing);
    }

    @Test
    void shouldReturnWithNoErrorsWhenSelectedIdIsNull() {
        when(cicCaseHearingLabel.getLabel()).thenReturn(null);
        when(cicCaseHearingList.getValue()).thenReturn(cicCaseHearingLabel);
        when(cicCase.getHearingList()).thenReturn(cicCaseHearingList);

        final AboutToStartOrSubmitResponse<CaseData, State> response = selectHearing.midEvent(caseDetails, caseDetails);

        assertThat(response.getData().getListing()).isEqualTo(initialListing);
        assertThat(response.getData().getListing()).isNotEqualTo(finalListingListValue.getValue());
    }
}
