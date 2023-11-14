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
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.ListingCreatedNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_RECORD_LISTING;

@ExtendWith(MockitoExtension.class)
class CaseworkerRecordListingTest {

    @Mock
    private HearingService hearingService;

    @Mock
    private RecordListHelper recordListHelper;

    @InjectMocks
    private CaseworkerRecordListing caseworkerRecordListing;

    @Mock
    private ListingCreatedNotification listingCreatedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerRecordListing.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RECORD_LISTING);
    }

    @Test
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        parties.add(NotificationParties.APPLICANT);
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        Listing listing = getRecordListing();
        caseData.setListing(listing);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        Mockito.doNothing().when(listingCreatedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(listingCreatedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(listingCreatedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        when(recordListHelper.checkAndUpdateVenueInformation(any())).thenReturn(listing);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerRecordListing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getListing().getHearingType().getLabel()).isEqualTo("Final");
        assertThat(response.getData().getListing().getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(stayedResponse).isNotNull();
    }

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        caseworkerRecordListing.aboutToStart(updatedCaseDetails);

        //Then
        verify(recordListHelper).regionData(caseData);
    }

    @Test
    void shouldNotPopulateRegionDataInAboutToStartCallbackIfAlreadyPresent() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().setRegionList(getMockedRegionData());
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        caseworkerRecordListing.aboutToStart(updatedCaseDetails);

        //Then
        verifyNoInteractions(recordListHelper);
    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenueData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing listing = new Listing();
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
        caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        verify(recordListHelper).populateVenuesData(caseData);
    }

    @Test
    void shouldNotPopulateHearingVenueDataInMidEventCallbackIfAlreadyPresent() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing recordListing = new Listing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setRegionList(getMockedRegionData());
        recordListing.setHearingVenues(getMockedHearingVenueData());
        caseData.setListing(recordListing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        verifyNoInteractions(recordListHelper);
    }

    @Test
    void shouldReturnErrorsIfAllNotificationPartiesSelected() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        Listing recordListing = getRecordListing();
        caseData.setListing(recordListing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(recordListHelper.checkAndUpdateVenueInformation(any())).thenReturn(recordListing);

        AboutToStartOrSubmitResponse<CaseData, State> response
            = caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getCicCase().getHearingNotificationParties()).hasSize(4);
        assertThat(response.getData().getCicCase().getHearingNotificationParties()).contains(NotificationParties.SUBJECT);
        assertThat(response.getData().getCicCase().getHearingNotificationParties()).contains(NotificationParties.SUBJECT);
    }

    private CicCase getMockCicCase() {
        return CicCase.builder().fullName("fullName").notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .representativeFullName("repFullName").notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .respondentName("respName").notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
    }


    private DynamicList getMockedRegionData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("1-region")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getMockedHearingVenueData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("courtname-courtAddress")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

}
