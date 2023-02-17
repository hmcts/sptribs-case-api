package uk.gov.hmcts.sptribs.common.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.TestConstants;
import uk.gov.hmcts.sptribs.testutil.TestEventConstants;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_2;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_3;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_4;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_5;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_6;
import static uk.gov.hmcts.sptribs.common.CommonConstants.ADDRESS_LINE_7;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;

@ExtendWith(MockitoExtension.class)
public class NotificationHelperTest {

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void setRecordingTemplateVarsTest() {
        //Given
        RecordListing recordListing = RecordListing.builder()
            .conferenceCallNumber("cmi459t5iut5")
            .hearingDate(LocalDate.of(2022, 12, 23))
            .importantInfoDetails("Imp Info")
            .videoCallLink("http://abc.com")
            .conferenceCallNumber("+56677778")
            .hearingFormat(HearingFormat.FACE_TO_FACE)
            .build();
        Map<String, Object> templateVars = new HashMap<>();


        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE)).isEqualTo(true);
    }

    @Test
    void setRecordingTemplateVarsTest_VideoFormat() {
        //Given
        RecordListing recordListing = RecordListing.builder()
            .hearingDate(LocalDate.of(2022, 12, 23))
            .hearingFormat(HearingFormat.VIDEO)
            .build();
        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO)).isEqualTo(true);
    }

    @Test
    void setRecordingTemplateVarsTest_HearingFormat_null() {
        //Given
        RecordListing recordListing = RecordListing.builder()
            .hearingDate(LocalDate.of(2022, 12, 23))
            .build();
        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }

    @Test
    void setRecordingTemplateVarsTest_SelectedVenueSet() {
        //Given
        RecordListing recordListing = Mockito.mock(RecordListing.class);

        when(recordListing.getSelectedVenue()).thenReturn("London Hearing Venue");
        when(recordListing.getHearingDate()).thenReturn(LocalDate.of(2022, 12, 23));
        when(recordListing.getHearingFormat()).thenReturn(HearingFormat.HYBRID);

        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }

    @Test
    void setRecordingTemplateVarsTest_ManualHearingVenueSet() throws IOException {
        //Given
        RecordListing recordListing = Mockito.mock(RecordListing.class);

        when(recordListing.getHearingVenueNameAndAddress()).thenReturn("London Hearing Venue - London");
        when(recordListing.getHearingFormat()).thenReturn(HearingFormat.HYBRID);
        when(recordListing.getHearingDate()).thenReturn(LocalDate.of(2022, 12, 23));
        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }

    @Test
    void setRecordingTemplateVarsTest_TelephoneFormat() {
        //Given
        RecordListing recordListing = RecordListing.builder()
            .hearingDate(LocalDate.of(2022, 12, 23))
            .conferenceCallNumber("cmi459t5iut5")
            .addlInstr("Test Instructions")
            .videoCallLink("http://abc.com")
            .conferenceCallNumber("+56677778")
            .hearingFormat(HearingFormat.TELEPHONE)
            .build();

        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL)).isEqualTo(true);
    }

    @Test
    void shouldGetRespondentCommonVars() {
        // Given
        CicCase cicCase = CicCase.builder()
            .respondentName("respondent name")
            .build();

        // When
        Map<String, Object> commonVars = notificationHelper.getRespondentCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("respondent name");
    }

    @Test
    void shouldGetSubjectCommonVars() {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("subject name")
            .build();

        // When
        Map<String, Object> commonVars = notificationHelper.getSubjectCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("subject name");
    }

    @Test
    void shouldGetApplicantCommonVars() {
        // Given
        CicCase cicCase = CicCase.builder()
            .applicantFullName("app name")
            .build();

        // When
        Map<String, Object> commonVars = notificationHelper.getApplicantCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("app name");
    }

    @Test
    void shouldGetReprCommonVars() {
        // Given
        CicCase cicCase = CicCase.builder()
            .representativeFullName("repr name")
            .build();

        // When
        Map<String, Object> commonVars = notificationHelper.getRepresentativeCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("repr name");
    }

    @Test
    void shouldGetAddressVars() {
        // Given
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("test addr1")
            .addressLine2("test addr2")
            .addressLine3("test addr3")
            .postCode("test postcode")
            .county("test county")
            .country("test county")
            .postTown("test postTown")
            .build();
        Map<String, Object> templateVars = new HashMap<>();

        // When
        notificationHelper.addAddressTemplateVars(addressGlobalUK, templateVars);

        // Then
        assertThat(templateVars.get(ADDRESS_LINE_1)).isEqualTo("test addr1");
        assertThat(templateVars.get(ADDRESS_LINE_2)).isEqualTo("test addr2");
        assertThat(templateVars.get(ADDRESS_LINE_3)).isEqualTo("test addr3");
        assertThat(templateVars.get(ADDRESS_LINE_4)).isEqualTo("test postTown");
        assertThat(templateVars.get(ADDRESS_LINE_5)).isEqualTo("test county");
        assertThat(templateVars.get(ADDRESS_LINE_6)).isEqualTo("test county");
        assertThat(templateVars.get(ADDRESS_LINE_7)).isEqualTo("test postcode");
    }

    @Test
    void shouldBuildNotificationRequest() {
        // When
        NotificationRequest emailNotificationRequest = notificationHelper.buildEmailNotificationRequest(
            "id@email.com",
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        NotificationRequest emailNotificationRequestWithAttachment = notificationHelper.buildEmailNotificationRequest(
            "id@email.com",
            false,
            new ArrayList<>(),
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        NotificationRequest letterNotificationRequest = notificationHelper.buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);

        // Then
        assertThat(emailNotificationRequest).isNotNull();
        assertThat(emailNotificationRequestWithAttachment).isNotNull();
        assertThat(letterNotificationRequest).isNotNull();
    }

    @Test
    void shouldAddHearingPostponedTemplateVars() {
        Map<String, Object> templateVars = new HashMap<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(getDynamicList())
            .build();

        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        // Then
        assertThat(templateVars.get(HEARING_DATE)).isEqualTo(LocalDate.now().toString());
        assertThat(templateVars.get(HEARING_TIME)).isEqualTo("11:00");
    }

    private DynamicList getDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("HearingType" + SPACE + HYPHEN + SPACE + HEARING_DATE_1 + TestEventConstants.SPACE + TestConstants.HEARING_TIME)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
