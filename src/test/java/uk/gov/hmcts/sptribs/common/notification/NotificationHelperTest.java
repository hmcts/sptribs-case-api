package uk.gov.hmcts.sptribs.common.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;

@ExtendWith(MockitoExtension.class)
public class NotificationHelperTest {

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void setRecordingTemplateVarsTest() throws IOException {
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
    void setRecordingTemplateVarsTest_VideoFormat() throws IOException {
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
    void setRecordingTemplateVarsTest_HearingFormat_null() throws IOException {
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
    void setRecordingTemplateVarsTest_SelectedVenueSet() throws IOException {
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

        when(recordListing.getHearingVenueName()).thenReturn("London Hearing Venue");
        when(recordListing.getHearingFormat()).thenReturn(HearingFormat.HYBRID);
        when(recordListing.getHearingDate()).thenReturn(LocalDate.of(2022, 12, 23));
        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
    }

    @Test
    void setRecordingTemplateVarsTest_TelephoneFormat() throws IOException {
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
}
