package uk.gov.hmcts.sptribs.caseworker.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;

@ExtendWith(MockitoExtension.class)
class RecordListHelperTest {

    public static final DynamicList EMPTY_DYNAMIC_LIST = DynamicListUtil.createDynamicList(Collections.emptyList());
    @InjectMocks
    private RecordListHelper recordListHelper;

    @Mock
    private LocationService locationService;

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(locationService.getAllRegions()).thenReturn(getMockedRegionData());
        recordListHelper.regionData(caseData);

        //Then
        assertThat(caseData.getListing().getRegionList().getValue().getLabel()).isEqualTo("1-region");
        assertThat(caseData.getListing().getRegionList().getListItems()).hasSize(1);
        assertThat(caseData.getListing().getRegionList().getListItems().get(0).getLabel()).isEqualTo("1-region");
        assertThat(caseData.getListing().getRegionsMessage()).isNull();
    }

    @ParameterizedTest
    @MethodSource("emptyAndNullDynamicListSource")
    void shouldSetRegionMessageToUnableToRetrieveWhenRegionsListIsNullOrEmpty(DynamicList regionList) {
        final CaseData caseData = caseData();

        when(locationService.getAllRegions()).thenReturn(regionList);
        recordListHelper.regionData(caseData);

        assertThat(caseData.getListing().getRegionsMessage()).contains("Unable to retrieve Region data");
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

        when(locationService.getRegionId("1-region")).thenReturn("1");
        when(locationService.getHearingVenuesByRegion("1")).thenReturn(getMockedHearingVenueData());
        recordListHelper.populateVenuesData(caseData);

        assertThat(caseData.getListing().getHearingVenues()
            .getValue().getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(caseData.getListing().getHearingVenues().getListItems()).hasSize(1);
        assertThat(caseData.getListing().getHearingVenues()
            .getListItems().get(0).getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(caseData.getListing().getHearingVenuesMessage()).isNull();
    }

    @ParameterizedTest
    @MethodSource("emptyAndNullDynamicListSource")
    void shouldSetHearingVenuesMessageToUnableToRetrieveWhenHearingVenueListIsNullOrEmpty(DynamicList dynamicList) {
        final CaseData caseData = caseData();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);

        when(locationService.getRegionId("1-region")).thenReturn("1");
        when(locationService.getHearingVenuesByRegion("1")).thenReturn((dynamicList));
        recordListHelper.populateVenuesData(caseData);

        assertThat(caseData.getListing().getHearingVenuesMessage()).contains("Unable to retrieve Hearing Venues data");
    }

    @Test
    void shouldNotSetHearingVenueWhenRegionIdIsNull() {
        final CaseData caseData = caseData();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);

        when(locationService.getRegionId("1-region")).thenReturn(null);
        recordListHelper.populateVenuesData(caseData);

        verify(locationService, never()).getHearingVenuesByRegion(any());
        assertThat(caseData.getListing().getHearingVenuesMessage()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cicCaseNotifyValues")
    void checkNullConditions(CicCase cicCase, boolean expected) {
        boolean result = recordListHelper.checkNullCondition(cicCase);
        assertThat(result).isEqualTo(expected);
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
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC));

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        recordListHelper.getNotificationParties(caseData);

        assertThat(caseData.getCicCase().getHearingNotificationParties()).hasSize(4);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.SUBJECT);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.REPRESENTATIVE);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.RESPONDENT);
        assertThat(caseData.getCicCase().getHearingNotificationParties()).contains(NotificationParties.APPLICANT);
        assertThat(caseData.getCicCase()).isNotNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformationVenueNotListed() {
        final Listing listing = Listing.builder()
            .hearingVenueNameAndAddress("name-address")
            .readOnlyHearingVenueName("name-address")
            .venueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED))
            .build();

        final Listing result = recordListHelper.checkAndUpdateVenueInformation(listing);

        assertThat(result.getReadOnlyHearingVenueName()).isNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformation() {
        final Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        final Listing listing = Listing.builder()
            .hearingVenueNameAndAddress("name-address")
            .readOnlyHearingVenueName("name-address")
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();

        final Listing result = recordListHelper.checkAndUpdateVenueInformation(listing);

        assertThat(result.getReadOnlyHearingVenueName()).isNotNull();
    }

    @Test
    void shouldSuccessfullyCheckAndUpdateVenueInformationSummary() {
        final Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        final Listing listing = Listing.builder()
            .readOnlyHearingVenueName("name-address")
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();

        final Listing result = recordListHelper.checkAndUpdateVenueInformationSummary(listing);

        assertThat(result.getHearingVenueNameAndAddress()).isNotNull();
    }

    @Test
    void shouldSuccessfullySaveSummary() {
        final Set<VenueNotListed> venueNotListedOption = new HashSet<>();
        final HearingSummary summary = HearingSummary.builder().build();
        final Listing listing = Listing.builder()
            .readOnlyHearingVenueName("name-address")
            .summary(summary)
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(venueNotListedOption)
            .build();
        final CaseData data = caseData();
        caseData().setListing(listing);

        final Listing result = recordListHelper.saveSummary(data);

        assertThat(result).isNotNull();
    }

    private static Stream<Arguments> emptyAndNullDynamicListSource() {
        return Stream.of(
            null,
            Arguments.arguments(EMPTY_DYNAMIC_LIST)
        );
    }

    private static Stream<Arguments> cicCaseNotifyValues() {
        final CicCase emptyCicCase = CicCase.builder().build();
        final CicCase notifySubject = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CicCase notifyRepresentative = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        final CicCase notifyRespondent = CicCase.builder()
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        final CicCase notifySubjectRepresentative = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        final CicCase notifySubjectRespondent= CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        final CicCase notifyRepresentativeRespondent =  CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();
        final CicCase notifyAll = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        return Stream.of(
            Arguments.arguments(null, true),
            Arguments.arguments(emptyCicCase, true),
            Arguments.arguments(notifySubject, false),
            Arguments.arguments(notifyRepresentative, false),
            Arguments.arguments(notifyRespondent, false),
            Arguments.arguments(notifySubjectRepresentative, false),
            Arguments.arguments(notifySubjectRespondent, false),
            Arguments.arguments(notifyRepresentativeRespondent, false),
            Arguments.arguments(notifyAll, false)
        );
    }
}
