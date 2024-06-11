package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.ListingUpdatedNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_RECORD_LISTING;

@ExtendWith(MockitoExtension.class)
class CaseworkerEditRecordListingTest {

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private CaseworkerEditRecordListing caseworkerEditRecordList;

    @Mock
    private RecordListHelper recordListHelper;

    @Mock
    private ListingUpdatedNotification listingUpdatedNotification;

    @Mock
    private CaseData caseDataBefore;

    @Mock
    private CaseData caseDataAfter;
    @Mock
    private Listing listingBefore;
    @Mock
    private Listing listingAfter;
    @Mock
    private DynamicList dynamicListMock;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerEditRecordList.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_RECORD_LISTING);
    }

    @Test
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        Set<NotificationParties> hearingNotificationPartiesSet = new HashSet<>();
        hearingNotificationPartiesSet.add(NotificationParties.SUBJECT);
        hearingNotificationPartiesSet.add(NotificationParties.REPRESENTATIVE);
        hearingNotificationPartiesSet.add(NotificationParties.RESPONDENT);
        Listing listing = getRecordListing();
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CicCase cicCase = getMockCicCase();
        cicCase.setHearingNotificationParties(hearingNotificationPartiesSet);
        cicCase.setHearingList(DynamicList.builder()
                .value(DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build())
                .build()
        );
        caseData.setListing(listing);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setCurrentEvent("");
        Mockito.doNothing().when(listingUpdatedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(listingUpdatedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(listingUpdatedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        when(recordListHelper.checkAndUpdateVenueInformation(any())).thenReturn(listing);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);


        SubmittedCallbackResponse stayedResponse = caseworkerEditRecordList.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getListing().getHearingType().getLabel()).isEqualTo("Final");
        assertThat(response.getData().getListing().getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(stayedResponse).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        final Listing listing = Listing.builder()
            .readOnlyHearingVenueName("asa")
            .hearingVenueNameAndAddress("asa")
            .build();
        caseData.setListing(listing);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordList.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response.getState().getName()).isEqualTo("CaseManagement");
        assertThat(response.getData().getListing().getHearingVenueNameAndAddress()).isNull();

    }

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionDataCheck() {
        //Given
        final CaseData caseData = caseData();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordList.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response.getState().getName()).isEqualTo("CaseManagement");

    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenueData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        recordListHelper.regionData(caseData);

        if (beforeDetails.getData() == null) {
            beforeDetails.setData(updatedCaseDetails.getData());
        }

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(caseData.getListing().getSelectedRegionVal()).isNotNull();
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenues() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        DynamicList hearingVenueList = getMockedHearingVenueData();
        when(caseDataBefore.getListing()).thenReturn(listingBefore);
        when(caseDataAfter.getListing()).thenReturn(listingAfter);
        when(listingBefore.getReadOnlyHearingVenueName()).thenReturn("Read Only Hearing Venue Name");
        when(listingBefore.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name");
        when(listingBefore.getHearingVenues()).thenReturn(hearingVenueList);
        when(listingAfter.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name");
        when(listingAfter.getHearingVenues()).thenReturn(dynamicListMock);
        updatedCaseDetails.setData(caseDataAfter);
        beforeDetails.setData(caseDataBefore);
        //When
        caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);
        //Then
        verify(listingAfter, times(1)).setHearingVenues(any(DynamicList.class));
        verify(dynamicListMock, times(1)).setValue(any(DynamicListElement.class));
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenuesWhenHearingNameIsNull() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        when(caseDataBefore.getListing()).thenReturn(listingBefore);
        when(listingBefore.getReadOnlyHearingVenueName()).thenReturn(null);
        updatedCaseDetails.setData(caseDataAfter);
        beforeDetails.setData(caseDataBefore);
        //When
        caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);
        //Then
        verify(listingAfter, times(0)).setHearingVenues(any(DynamicList.class));
        verify(dynamicListMock, times(0)).setValue(any(DynamicListElement.class));
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenuesWhenRegionIsNull() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        when(caseDataBefore.getListing()).thenReturn(listingBefore);
        when(caseDataAfter.getListing()).thenReturn(listingAfter);
        when(listingBefore.getReadOnlyHearingVenueName()).thenReturn("Read Only Hearing Venue Name");
        when(listingAfter.getSelectedRegionVal()).thenReturn(null);
        updatedCaseDetails.setData(caseDataAfter);
        beforeDetails.setData(caseDataBefore);
        //When
        caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);
        //Then
        verify(listingAfter, times(0)).setHearingVenues(any(DynamicList.class));
        verify(dynamicListMock, times(0)).setValue(any(DynamicListElement.class));
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenuesWhenRegionValChanges() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        when(caseDataBefore.getListing()).thenReturn(listingBefore);
        when(caseDataAfter.getListing()).thenReturn(listingAfter);
        when(listingBefore.getReadOnlyHearingVenueName()).thenReturn("Read Only Hearing Venue Name");
        when(listingBefore.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name");
        when(listingAfter.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name DIFFERENT");
        updatedCaseDetails.setData(caseDataAfter);
        beforeDetails.setData(caseDataBefore);
        //When
        caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);
        //Then
        verify(listingAfter, times(0)).setHearingVenues(any(DynamicList.class));
        verify(dynamicListMock, times(0)).setValue(any(DynamicListElement.class));
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenuesWhenHearingVenuesAreNull() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        when(caseDataBefore.getListing()).thenReturn(listingBefore);
        when(caseDataAfter.getListing()).thenReturn(listingAfter);
        when(listingBefore.getReadOnlyHearingVenueName()).thenReturn("Read Only Hearing Venue Name");
        when(listingBefore.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name");
        when(listingAfter.getSelectedRegionVal()).thenReturn("Read Only Hearing Venue Name");
        updatedCaseDetails.setData(caseDataAfter);
        beforeDetails.setData(caseDataBefore);
        //When
        caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);
        //Then
        verify(listingAfter, times(0)).setHearingVenues(any(DynamicList.class));
        verify(dynamicListMock, times(0)).setValue(any(DynamicListElement.class));
    }

    @Test
    void shouldNotReturnErrorsIfCaseDataIsValid() {
        final CaseData caseData = caseData();

        caseData.getCicCase().setNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        caseData.getCicCase().setHearingList(DynamicList.builder()
            .value(DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build())
            .build()
        );
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void shouldHearingVenueEqualIfRegionValIsEqual() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        beforeDetails.setData(updatedCaseDetails.getData());

        recordListHelper.regionData(caseData);
        recordListHelper.populateVenuesData(caseData);


        if (caseData.getListing().getSelectedRegionVal().equals(beforeDetails.getData().getListing().getSelectedRegionVal())) {
            caseData.getListing().setHearingVenues(beforeDetails.getData().getListing().getHearingVenues());

        }
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getData()).isEqualTo(beforeDetails.getData());
        assertThat(beforeDetails.getData().getListing().getRegionList()).isNotNull();
    }

    @Test
    void shouldNotReturnErrorsIfEditCaseDataIsValid() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        caseData.setCurrentEvent(CASEWORKER_EDIT_RECORD_LISTING);
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        if (beforeDetails.getData() == null) {
            beforeDetails.setData(updatedCaseDetails.getData());
        }

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).isNull();

    }

    @Test
    void shouldReturnErrorsIfCaseDataIsNull() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getCicCase().setHearingList(DynamicList.builder()
            .value(DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build())
            .build()
        );
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(recordListHelper.getErrorMsg(any())).thenReturn(List.of("One party must be selected."));
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);

    }

    @Test
    void shouldChangeStateOnAboutToSubmit() {
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(
                DynamicList.builder()
                    .value(DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build())
                    .build()
            )
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getState().getName()).isEqualTo("AwaitingHearing");
    }

    private CicCase getMockCicCase() {
        return CicCase.builder().fullName("fullName").notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .representativeFullName("repFullName").notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .respondentName("respName").notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
    }
}
