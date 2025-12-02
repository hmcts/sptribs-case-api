package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.ListingUpdatedNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.POST;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingType.FINAL;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_HEARING_TIME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_HEARING_TYPE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_UPDATED_EMAIL;
import static uk.gov.hmcts.sptribs.notification.TemplateName.HEARING_UPDATED_POST;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ListingUpdatedNotificationIT {

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @Autowired
    private ListingUpdatedNotification listingUpdatedNotification;

    @Captor
    ArgumentCaptor<NotificationRequest> notificationRequestCaptor;

    @Test
    void shouldSendEmailToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(EMAIL)
                .fullName("Subject Name")
                .email("test@email.com")
                .build())
            .listing(getRecordListing())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name",
            CIC_CASE_HEARING_TYPE, FINAL,
            CIC_CASE_HEARING_DATE, "21-04-2023",
            CIC_CASE_HEARING_TIME, "10:00",
            CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true
        );

        listingUpdatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_UPDATED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendLetterToSubject() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .contactPreferenceType(POST)
                .fullName("Subject Name")
                .address(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .listing(getRecordListing())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Subject Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW",
            CIC_CASE_HEARING_TYPE, FINAL,
            CIC_CASE_HEARING_DATE, "21-04-2023",
            CIC_CASE_HEARING_TIME, "10:00",
            CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true
        );

        listingUpdatedNotification.sendToSubject(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_UPDATED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .representativeFullName("Representative Name")
                .representativeEmailAddress("test@email.com")
                .build())
            .listing(getRecordListing())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name",
            CIC_CASE_HEARING_TYPE, FINAL,
            CIC_CASE_HEARING_DATE, "21-04-2023",
            CIC_CASE_HEARING_TIME, "10:00",
            CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true
        );

        listingUpdatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_UPDATED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendLetterToRepresentative() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeContactDetailsPreference(POST)
                .fullName("Subject Name")
                .representativeFullName("Representative Name")
                .representativeAddress(AddressGlobalUK.builder()
                    .addressLine1("10 Buckingham Palace")
                    .postCode("W1 1BW")
                    .build()
                )
                .build())
            .listing(getRecordListing())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Representative Name",
            ADDRESS_LINE_1, "10 Buckingham Palace",
            ADDRESS_LINE_7, "W1 1BW",
            CIC_CASE_HEARING_TYPE, FINAL,
            CIC_CASE_HEARING_DATE, "21-04-2023",
            CIC_CASE_HEARING_TIME, "10:00",
            CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true
        );

        listingUpdatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendLetter(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_UPDATED_POST);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }

    @Test
    void shouldSendEmailToRespondent() {
        final CaseData data = CaseData.builder()
            .cicCase(CicCase.builder()
                .representativeContactDetailsPreference(EMAIL)
                .fullName("Subject Name")
                .respondentName("Respondent Name")
                .respondentEmail("test@email.com")
                .build())
            .listing(getRecordListing())
            .build();

        final Map<String, Object> expectedTemplateVars = Map.of(
            TRIBUNAL_NAME, CIC,
            CIC_CASE_NUMBER, TEST_CASE_ID.toString(),
            CIC_CASE_SUBJECT_NAME, "Subject Name",
            CONTACT_NAME, "Respondent Name",
            CIC_CASE_HEARING_TYPE, FINAL,
            CIC_CASE_HEARING_DATE, "21-04-2023",
            CIC_CASE_HEARING_TIME, "10:00",
            CIC_CASE_RECORD_HEARING_1FACE_TO_FACE, true
        );

        listingUpdatedNotification.sendToRespondent(data, TEST_CASE_ID.toString());

        verify(notificationServiceCIC).sendEmail(notificationRequestCaptor.capture(), eq(TEST_CASE_ID.toString()));

        NotificationRequest notificationRequest = notificationRequestCaptor.getValue();

        assertThat(notificationRequest.getDestinationAddress())
            .isEqualTo("test@email.com");
        assertThat(notificationRequest.getTemplate())
            .isEqualTo(HEARING_UPDATED_EMAIL);
        assertThat(notificationRequest.getTemplateVars())
            .containsAllEntriesOf(expectedTemplateVars);
    }
}
