package uk.gov.hmcts.sptribs.common.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import static uk.gov.hmcts.sptribs.common.CommonConstants.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.NO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.YES;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;

@ExtendWith(MockitoExtension.class)
public class NotificationHelperTest {

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Mock
    private CicCase cicCase;

    @Test
    void setRecordingTemplateVarsTest_AllConditionsMet() {
        //Given
        final Listing listing = Listing.builder()
            .conferenceCallNumber("cmi459t5iut5")
            .date(LocalDate.of(2022, 12, 23))
            .importantInfoDetails("Imp Info")
            .videoCallLink("http://abc.com")
            .conferenceCallNumber("+56677778")
            .hearingFormat(HearingFormat.VIDEO)
            .addlInstr("Test Instructions")
            .build();

        final Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        assertEquals(10, templateVars.size());
        assertEquals(true, templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO));
        assertEquals(CommonConstants.CIC_CASE_RECORD_REMOTE_HEARING, templateVars.get(CommonConstants.CIC_CASE_HEARING_VENUE));
        assertEquals(templateVars.get(CommonConstants.CIC_CASE_HEARING_INFO), listing.getAddlInstr());
        assertEquals(templateVars.get(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK), listing.getVideoCallLink());
        assertEquals(templateVars.get(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM), listing.getConferenceCallNumber());

    }

    @Test
    void setRecordingTemplateVarsTest_AllConditionNotMet() {
        //Given
        final Listing listing = Listing.builder()
            .date(LocalDate.of(2022, 12, 23))
            .build();
        final Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        assertEquals(10, templateVars.size());
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO));
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE));
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL));
        assertEquals(" ", templateVars.get(CommonConstants.CIC_CASE_HEARING_VENUE));
        assertEquals(" ", templateVars.get(CommonConstants.CIC_CASE_HEARING_INFO));
        assertEquals(" ", templateVars.get(CommonConstants.CIC_CASE_RECORD_VIDEO_CALL_LINK));
        assertEquals(" ", templateVars.get(CommonConstants.CIC_CASE_RECORD_CONF_CALL_NUM));
    }

    @Test
    void setRecordingTemplateVarsTest_TelephoneFormat() {
        //Given
        final Listing listing = Listing.builder()
            .date(LocalDate.of(2022, 12, 23))
            .conferenceCallNumber("cmi459t5iut5")
            .addlInstr("Test Instructions")
            .videoCallLink("http://abc.com")
            .conferenceCallNumber("+56677778")
            .hearingFormat(HearingFormat.TELEPHONE)
            .build();

        final Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL)).isEqualTo(true);
    }

    @Test
    void setRecordingTemplateVarsTest_FacetoFaceFormat() {
        //Given
        final Listing listing = Listing.builder()
            .date(LocalDate.of(2022, 12, 23))
            .hearingFormat(HearingFormat.FACE_TO_FACE)
            .build();
        final Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE)).isEqualTo(true);
    }


    @Test
    void setRecordingTemplateVarsTest_SelectedVenueSet() {
        //Given
        final Listing listing = Mockito.mock(Listing.class);

        when(listing.getSelectedVenue()).thenReturn("London Hearing Venue");
        when(listing.getDate()).thenReturn(LocalDate.of(2022, 12, 23));
        when(listing.getHearingFormat()).thenReturn(HearingFormat.HYBRID);

        final Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }

    @Test
    void setRecordingTemplateVarsTest_ManualHearingVenueSet() {
        //Given
        final Listing listing = Mockito.mock(Listing.class);

        when(listing.getHearingVenueNameAndAddress()).thenReturn("London Hearing Venue - London");
        when(listing.getSelectedVenue()).thenReturn(null);
        when(listing.getDate()).thenReturn(LocalDate.of(2022, 12, 23));
        Map<String, Object> templateVars = new HashMap<>();

        //When
        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }


    @Test
    void shouldSetRecordingTemplateVarsWithVenueNull() {

        final Listing listing = Listing.builder()
            .date(LocalDate.of(2022, 12, 23))
            .conferenceCallNumber("cmi459t5iut5")
            .addlInstr("Test Instructions")
            .videoCallLink("http://abc.com")
            .conferenceCallNumber("+56677778")
            .hearingVenues(null)
            .hearingVenueNameAndAddress(null)
            .addlInstr("Test Instructions")
            .build();

        final Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, listing);

        assertEquals(10, templateVars.size());
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO));
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_1FACE_TO_FACE));
        assertEquals(false, templateVars.get(CommonConstants.CIC_CASE_RECORD_FORMAT_TEL));
        assertEquals(" ", templateVars.get(CommonConstants.CIC_CASE_HEARING_VENUE));
    }


    @Test
    void shouldGetRespondentCommonVars() {
        // Given
        final CicCase cicCase = CicCase.builder()
            .respondentName("respondent name")
            .build();

        // When
        final Map<String, Object> commonVars = notificationHelper.getRespondentCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("respondent name");
    }


    @Test
    void shouldGetTribunalCommonVars() {
        // Given
        final CicCase cicCase = CicCase.builder()
            .build();

        // When
        final Map<String, Object> commonVars = notificationHelper.getTribunalCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("First-tier Tribunal (CIC)");
    }

    @Test
    void shouldGetSubjectCommonVars() {
        // Given
        final CicCase cicCase = CicCase.builder()
            .fullName("subject name")
            .build();

        // When
        final Map<String, Object> commonVars = notificationHelper.getSubjectCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("subject name");
    }

    @Test
    void shouldGetApplicantCommonVars() {
        // Given
        final CicCase cicCase = CicCase.builder()
            .applicantFullName("app name")
            .build();

        // When
        final Map<String, Object> commonVars = notificationHelper.getApplicantCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("app name");
    }

    @Test
    void shouldGetReprCommonVars() {
        // Given
        final CicCase cicCase = CicCase.builder()
            .representativeFullName("repr name")
            .build();

        // When
        final Map<String, Object> commonVars = notificationHelper.getRepresentativeCommonVars("case number", cicCase);

        // Then
        assertThat(commonVars.get(CONTACT_NAME)).isEqualTo("repr name");
    }

    @Test
    void shouldGetAddressVars() {
        // Given
        final AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("test addr1")
            .addressLine2("test addr2")
            .addressLine3("test addr3")
            .postCode("test postcode")
            .county("test county")
            .country("test county")
            .postTown("test postTown")
            .build();
        final Map<String, Object> templateVars = new HashMap<>();

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
        final NotificationRequest emailNotificationRequest = notificationHelper.buildEmailNotificationRequest(
            "id@email.com",
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        final NotificationRequest emailNotificationRequestWithAttachment = notificationHelper.buildEmailNotificationRequest(
            "id@email.com",
            false,
            new HashMap<>(),
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);
        final NotificationRequest letterNotificationRequest = notificationHelper.buildLetterNotificationRequest(
            new HashMap<>(),
            TemplateName.CASE_ISSUED_CITIZEN_EMAIL);

        // Then
        assertThat(emailNotificationRequest).isNotNull();
        assertThat(emailNotificationRequestWithAttachment).isNotNull();
        assertThat(letterNotificationRequest).isNotNull();
    }

    @Test
    void shouldAddHearingPostponedTemplateVars() {
        final Map<String, Object> templateVars = new HashMap<>();
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

    @Test
    void shouldAddHearingPostponedTemplateVarsWithNull() {
        final Map<String, Object> templateVars = new HashMap<>();
        when(cicCase.getSelectedHearingToCancel()).thenReturn(null);

        notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars);

        assertNull(templateVars.get(HEARING_DATE));
        assertNull(templateVars.get(HEARING_TIME));
    }

    @Test
    void shouldTryToAddHearingPostponedTemplateVarsWithInvalidFormat() {

        final Map<String, Object> templateVars = new HashMap<>();

        final String invalidFormat = "13/04/2024";

        when(cicCase.getSelectedHearingToCancel()).thenReturn(invalidFormat);

        assertThrows(StringIndexOutOfBoundsException.class, () ->
            notificationHelper.addHearingPostponedTemplateVars(cicCase, templateVars));

        assertNull(templateVars.get(HEARING_DATE));
        assertNull(templateVars.get(HEARING_TIME));
    }

    @Test
    void shouldBuildDocumentList() {
        //Given
        final String documentLabel =
            "[Document 1.pdf A - First decision](http://exui.net/documents/5e32a0d2-9b37-4548-b007-b9b2eb580d0a/binary)";
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(documentLabel)
            .code(UUID.randomUUID())
            .build();

        final List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(listItem);

        final DynamicMultiSelectList documentList = new DynamicMultiSelectList();
        documentList.setListItems(listItems);
        documentList.setValue(listItems);

        final int docAttachLimit = 2;

        //When
        final Map<String, String> result = notificationHelper.buildDocumentList(documentList, docAttachLimit);

        //Then
        assertThat(result)
            .isNotNull()
            .hasSize(4)
            .containsKey("CaseDocument1")
            .containsKey("CaseDocument2")
            .containsKey("DocumentAvailable1")
            .containsKey("DocumentAvailable2")
            .containsEntry("CaseDocument1", "5e32a0d2-9b37-4548-b007-b9b2eb580d0a")
            .containsEntry("CaseDocument2", EMPTY_PLACEHOLDER)
            .containsEntry("DocumentAvailable1", YES)
            .containsEntry("DocumentAvailable2", NO);
    }

    @Test
    void shouldBuildDocumentListWhereListHasEmptyItems() {
        //Given
        final DynamicMultiSelectList documentList = new DynamicMultiSelectList();
        documentList.setListItems((Collections.emptyList()));

        final int docAttachLimit = 2;

        //When
        final Map<String, String> result = notificationHelper.buildDocumentList(documentList, docAttachLimit);

        //Then
        assertThat(result)
            .isNotNull()
            .hasSize(4)
            .containsKey("CaseDocument1")
            .containsKey("CaseDocument2")
            .containsKey("DocumentAvailable1")
            .containsKey("DocumentAvailable2")
            .containsEntry("CaseDocument1", EMPTY_PLACEHOLDER)
            .containsEntry("CaseDocument2", EMPTY_PLACEHOLDER)
            .containsEntry("DocumentAvailable1", NO)
            .containsEntry("DocumentAvailable2", NO);
    }

    @Test
    void shouldBuildDocumentListWhereListIsNull() {
        //Given
        final DynamicMultiSelectList documentList = new DynamicMultiSelectList();
        documentList.setListItems((null));

        final int docAttachLimit = 2;

        //When
        final Map<String, String> result = notificationHelper.buildDocumentList(documentList, docAttachLimit);

        //Then
        assertThat(result)
            .isNotNull()
            .hasSize(4)
            .containsKey("CaseDocument1")
            .containsKey("CaseDocument2")
            .containsKey("DocumentAvailable1")
            .containsKey("DocumentAvailable2")
            .containsEntry("CaseDocument1", EMPTY_PLACEHOLDER)
            .containsEntry("CaseDocument2", EMPTY_PLACEHOLDER)
            .containsEntry("DocumentAvailable1", NO)
            .containsEntry("DocumentAvailable2", NO);
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
