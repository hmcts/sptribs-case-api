package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void shouldReturnHearingSelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final List<ListValue<Listing>> listValueList = new ArrayList<>();

        final ListValue<Listing> finalListingListValue = new ListValue<>();
        finalListingListValue.setValue(getRecordListing());
        finalListingListValue.setId("Final 10:00");
        listValueList.add(finalListingListValue);

        final Listing caseManagementListing = getRecordListing();
        caseManagementListing.setHearingType(HearingType.CASE_MANAGEMENT);
        final ListValue<Listing> caseManagementListingListValue = new ListValue<>();
        caseManagementListingListValue.setValue(caseManagementListing);
        caseManagementListingListValue.setId("Case Management 10:00");
        listValueList.add(caseManagementListingListValue);

        when(cicCaseHearingLabel.getLabel()).thenReturn("Final 10:00");
        when(cicCaseHearingList.getValue()).thenReturn(cicCaseHearingLabel);
        when(cicCase.getHearingList()).thenReturn(cicCaseHearingList);

        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(cicCase);
        caseData.setListing(caseManagementListing);
        caseData.setHearingList(listValueList);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = selectHearing.midEvent(caseDetails, caseDetails);

        Assertions.assertEquals(response.getData().getListing(), finalListingListValue.getValue());
    }

}
