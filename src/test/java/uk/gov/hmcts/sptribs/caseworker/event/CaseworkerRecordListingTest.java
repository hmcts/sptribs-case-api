package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRecordListing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RECORD_LISTING);

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

    @Test
    void submittedShouldThrowExceptionWhenSendIsUnsuccessful() {
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

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);

        doAnswer(invocation -> {
            NotificationContext context = invocation.getArgument(0);
            if (context.getNotification() == listingCreatedNotification) {
                context.getErrors().add("Representative");
                context.getErrors().add("Applicant");
                context.getErrors().add("Subject");
                context.getErrors().add("Respondent");
            }
            return null;
        }).when(notificationDispatcher).sendToCorrespondenceParties(any(NotificationContext.class));

        final SubmittedCallbackResponse response = caseworkerRecordListing.submitted(updatedCaseDetails, beforeCaseDetails);


        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader())
            .contains("# Create listing notification failed")
            .contains("## A notification could not be sent to:")
            .contains("Subject")
            .contains("Applicant")
            .contains("Respondent")
            .contains("Representative");

    }

    @Test
    void verifyNotificationIsSentToAllParties() {
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

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);

        doAnswer(invocation -> {
            NotificationContext context = invocation.getArgument(0);
            if (context.getNotification() == listingCreatedNotification) {
                context.getCorrespondenceParties().add(NotificationParties.REPRESENTATIVE);
                context.getCorrespondenceParties().add(NotificationParties.SUBJECT);
                context.getCorrespondenceParties().add(NotificationParties.APPLICANT);
                context.getCorrespondenceParties().add(NotificationParties.RESPONDENT);
            }
            return null;
        }).when(notificationDispatcher).sendToCorrespondenceParties(any(NotificationContext.class));

        final SubmittedCallbackResponse response = caseworkerRecordListing.submitted(updatedCaseDetails, beforeCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader())
            .contains("# Listing record created \n## A notification has been sent to: ")
            .contains("Subject")
            .contains("Applicant")
            .contains("Respondent")
            .contains("Representative");
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
