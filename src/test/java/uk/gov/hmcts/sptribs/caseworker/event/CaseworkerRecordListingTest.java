package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
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
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.notification.dispatcher.ListingCreatedNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.NO;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
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
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRecordListing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RECORD_LISTING);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(caseworkerRecordListing, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRecordListing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        final Set<NotificationParties> parties = new HashSet<>();
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
        final Listing listing = getRecordListing();
        caseData.setListing(listing);

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        doNothing().when(listingCreatedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(listingCreatedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(listingCreatedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(listingCreatedNotification).sendToApplicant(caseData, caseData.getHyphenatedCaseRef());
        when(recordListHelper.checkAndUpdateVenueInformation(any())).thenReturn(listing);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse stayedResponse = caseworkerRecordListing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        final Listing responseListing = response.getData().getListing();
        assertThat(responseListing.getHearingCreatedDate()).isEqualTo(LocalDate.now());
        assertThat(responseListing.getHearingType().getLabel()).isEqualTo("Final");
        assertThat(responseListing.getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(responseListing.getAdditionalHearingDate()).isNull();
        assertThat(responseListing.getPostponeReason()).isNull();
        assertThat(responseListing.getPostponeAdditionalInformation()).isNull();
        assertThat(responseListing.getRecordListingChangeReason()).isNull();
        assertThat(responseListing.getHearingCancellationReason()).isNull();
        assertThat(responseListing.getCancelHearingAdditionalDetail()).isNull();
        assertThat(response.getData().getStitchHearingBundleTask()).isEqualTo(NO);
        assertThat(response.getData().getCompleteHearingOutcomeTask()).isEqualTo(NO);

        HearingSummary summary = responseListing.getSummary();
        assertThat(summary.getJudge()).isNull();
        assertThat(summary.getIsFullPanel()).isNull();
        assertThat(summary.getMemberList()).isNull();
        assertThat(summary.getOutcome()).isNull();
        assertThat(summary.getAdjournmentReasons()).isNull();
        assertThat(summary.getOthers()).isNull();
        assertThat(summary.getOtherDetailsOfAdjournment()).isNull();
        assertThat(summary.getRecFile()).isNull();
        assertThat(summary.getRecDesc()).isNull();
        assertThat(summary.getRoles()).isNull();
        assertThat(summary.getSubjectName()).isNull();
        assertThat(stayedResponse).isNotNull();
    }

    @Test
    void aboutToStartMethodShouldSuccessfullyPopulateRegionData() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        caseworkerRecordListing.aboutToStart(updatedCaseDetails);

        verify(recordListHelper).regionData(caseData);
    }

    @Test
    void midEventMethodShouldSuccessfullyPopulateHearingVenueDataWhenNotPresent() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        listing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        listing.setRegionList(getMockedRegionData());
        caseData.setListing(listing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        verify(recordListHelper).populateVenuesData(caseData);
    }

    @Test
    void shouldNotPopulateHearingVenueDataInMidEventCallbackIfAlreadyPresent() {
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

        caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        verifyNoInteractions(recordListHelper);
    }

    @ParameterizedTest
    @EnumSource(NotificationParties.class)
    void submittedShouldThrowExceptionWhenSendIsUnsuccessful(NotificationParties notificationParty) {
        final CicCase cicCaseSubject = CicCase.builder()
            .hearingNotificationParties(Set.of(notificationParty))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCaseSubject)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();

        final Exception sendToException = new NotificationException(new Exception("Failed to send"));

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        switch (notificationParty) {
            case SUBJECT ->
                doThrow(sendToException).when(listingCreatedNotification).sendToSubject(any(CaseData.class), anyString());
            case REPRESENTATIVE ->
                doThrow(sendToException).when(listingCreatedNotification).sendToRepresentative(any(CaseData.class), anyString());
            case RESPONDENT ->
                doThrow(sendToException).when(listingCreatedNotification).sendToRespondent(any(CaseData.class), anyString());
            case APPLICANT ->
                doThrow(sendToException).when(listingCreatedNotification).sendToApplicant(any(CaseData.class), anyString());
            default -> doNothing();
        }

        final SubmittedCallbackResponse response = caseworkerRecordListing.submitted(updatedCaseDetails, beforeCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader())
            .isEqualTo(format("# Create listing notification failed %n## Please resend the notification"));
    }

    @ParameterizedTest
    @EnumSource(NotificationParties.class)
    void verifySendToIsCalledForEachParty(NotificationParties notificationParty) {
        final CicCase cicCaseSubject = CicCase.builder()
            .hearingNotificationParties(Set.of(notificationParty))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCaseSubject)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        switch (notificationParty) {
            case SUBJECT ->
                doNothing().when(listingCreatedNotification).sendToSubject(any(CaseData.class), anyString());
            case REPRESENTATIVE ->
                doNothing().when(listingCreatedNotification).sendToRepresentative(any(CaseData.class), anyString());
            case RESPONDENT ->
                doNothing().when(listingCreatedNotification).sendToRespondent(any(CaseData.class), anyString());
            case APPLICANT ->
                doNothing().when(listingCreatedNotification).sendToApplicant(any(CaseData.class), anyString());
            default -> doNothing();
        }

        final SubmittedCallbackResponse response = caseworkerRecordListing.submitted(updatedCaseDetails, beforeCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader())
            .contains("# Listing record created \n## A notification has been sent to: ");

        switch (notificationParty) {
            case SUBJECT -> {
                verify(listingCreatedNotification, times(1)).sendToSubject(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRepresentative(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRespondent(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToApplicant(any(CaseData.class), anyString());
            }
            case REPRESENTATIVE -> {
                verify(listingCreatedNotification, times(0)).sendToSubject(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(1)).sendToRepresentative(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRespondent(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToApplicant(any(CaseData.class), anyString());
            }
            case RESPONDENT -> {
                verify(listingCreatedNotification, times(0)).sendToSubject(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRepresentative(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(1)).sendToRespondent(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToApplicant(any(CaseData.class), anyString());
            }
            case APPLICANT -> {
                verify(listingCreatedNotification, times(0)).sendToSubject(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRepresentative(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(0)).sendToRespondent(any(CaseData.class), anyString());
                verify(listingCreatedNotification, times(1)).sendToApplicant(any(CaseData.class), anyString());
            }
            default -> { }
        }
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
