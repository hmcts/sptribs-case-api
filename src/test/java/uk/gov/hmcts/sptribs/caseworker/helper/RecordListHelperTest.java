package uk.gov.hmcts.sptribs.caseworker.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;

@ExtendWith(MockitoExtension.class)
class RecordListHelperTest {

    @InjectMocks
    private RecordListHelper recordListHelper;

    @Mock
    private LocationService locationService;


    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        when(locationService.getAllRegions()).thenReturn(getMockedRegionData());
        recordListHelper.regionData(caseData);


        //Then
        assertThat(caseData.getListing().getRegionList().getValue().getLabel()).isEqualTo("1-region");
        assertThat(caseData.getListing().getRegionList().getListItems()).hasSize(1);
        assertThat(caseData.getListing().getRegionList().getListItems().get(0).getLabel()).isEqualTo("1-region");


    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenueData() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(locationService.getHearingVenuesByRegion("1")).thenReturn(getMockedHearingVenueData());
        recordListHelper.populatedVenuesData(caseData);

        //Then
        assertThat(caseData.getListing().getHearingVenues()
            .getValue().getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(caseData.getListing().getHearingVenues().getListItems()).hasSize(1);
        assertThat(caseData.getListing().getHearingVenues()
            .getListItems().get(0).getLabel()).isEqualTo("courtname-courtAddress");


    }

    @Test
    void shouldSuccessfullyCheckNullRecordNotifyParties() {
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();

        cicCase.setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        cicCase.setNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE));
        cicCase.setNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT));
        caseData.setCicCase(cicCase);

        recordListHelper.checkNullCondition(cicCase);
        recordListHelper.getErrorMsg(cicCase);

        assertThat(caseData.getCicCase().getNotifyPartySubject()).isNotNull();
        assertThat(caseData.getCicCase().getNotifyPartyRepresentative()).isNotNull();
        assertThat(caseData.getCicCase().getNotifyPartyRespondent()).isNotNull();
    }


    @Test
    void shouldSuccessfullyAddNotificationParties() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();

        caseData.getCicCase().setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT));

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        recordListHelper.getNotificationParties(caseData);

        assertThat(caseData.getCicCase().getHearingNotificationParties()).hasSize(3);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.SUBJECT);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.REPRESENTATIVE);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.RESPONDENT);
        assertThat(caseData.getCicCase()).isNotNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformationVenueNotListed() {

        Listing listing = Listing.builder()
            .hearingVenueNameAndAddress("name-address")
            .readOnlyHearingVenueName("name-address")
            .venueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED))
            .build();

        Listing result = recordListHelper.checkAndUpdateVenueInformation(listing);

        assertThat(result.getReadOnlyHearingVenueName()).isNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformation() {
        Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        Listing listing = Listing.builder()
            .hearingVenueNameAndAddress("name-address")
            .readOnlyHearingVenueName("name-address")
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();

        Listing result = recordListHelper.checkAndUpdateVenueInformation(listing);

        assertThat(result.getReadOnlyHearingVenueName()).isNotNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformationSummary() {
        Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        Listing listing = Listing.builder()
            .readOnlyHearingVenueName("name-address")
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();

        Listing result = recordListHelper.checkAndUpdateVenueInformationSummary(listing);

        assertThat(result.getHearingVenueNameAndAddress()).isNotNull();
    }

    @Test
    void shouldSuccessfullySaveSummary() {
        Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        HearingSummary summary = HearingSummary.builder().build();
        Listing listing = Listing.builder()
            .readOnlyHearingVenueName("name-address")
            .summary(summary)
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();
        CaseData data = caseData();
        caseData().setListing(listing);
        Listing result = recordListHelper.saveSummary(data);

        assertThat(result).isNotNull();
    }

}
