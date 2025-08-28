package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.elasticsearch.core.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListingUpdatedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private ListingUpdatedNotification listingUpdatedNotification;

    @Test
    void shouldNotifySubjectOfListingUpdatedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getEmail(),
            new HashMap<>(),
            TemplateName.HEARING_UPDATED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfListingUpdatedCitizenWithEmailWithFullRecordListing() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testSubject@outlook.com");
        final Listing listing = Listing.builder().hearingVenueNameAndAddress("London Centre - London")
                .conferenceCallNumber("cmi459t5iut5")
                    .videoCallLink("http://abc.com")
                        .conferenceCallNumber("+56677778")
                            .hearingFormat(HearingFormat.FACE_TO_FACE)
                                .build();
        data.setListing(listing);
        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            "testSubject@outlook.com",
            new HashMap<>(),
            TemplateName.HEARING_UPDATED_EMAIL);
    }

    @Test
    void shouldNotifySubjectOfListingUpdatedCitizenWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.HEARING_UPDATED_POST);
    }


    @Test
    void shouldNotifyRepresentativeOfListingUpdatedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRepresentativeEmailAddress(),
            Map.of(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME, data.getCicCase().getRepresentativeFullName()),
            TemplateName.HEARING_UPDATED_EMAIL);
    }

    @Test
    void shouldNotifyRepresentativeOfListingUpdatedCitizenWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(new AddressGlobalUK("11", "JOHN", "STREET", "WINCHESTER", "COUNTY", "TW4 5BH", "UK"));

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
        verify(notificationHelper).buildLetterNotificationRequest(
            Map.of(CommonConstants.CIC_CASE_REPRESENTATIVE_NAME,data.getCicCase().getRepresentativeFullName()),
            TemplateName.HEARING_UPDATED_POST);
    }

    @Test
    void shouldNotifyRespondentOfListingUpdatedCitizenWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("respFullName");
        data.getCicCase().setRespondentEmail("testRespondent@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        listingUpdatedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(notificationHelper).buildEmailNotificationRequest(
            data.getCicCase().getRespondentEmail(),
            Map.of(CommonConstants.CIC_CASE_RESPONDENT_NAME, data.getCicCase().getRespondentName()),
            TemplateName.HEARING_UPDATED_EMAIL);
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber("CN1").build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
