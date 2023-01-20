package uk.gov.hmcts.sptribs.common.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@ExtendWith(MockitoExtension.class)
public class NotificationHelperTest {

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void setRecordingTemplateVarsTest() throws IOException {
        //Given
        RecordListing recordListing = RecordListing.builder().hearingVenueName("London Centre")
            .conferenceCallNumber("cmi459t5iut5")
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
            .hearingFormat(HearingFormat.VIDEO)
            .build();
        Map<String, Object> templateVars = new HashMap<>();

        notificationHelper.setRecordingTemplateVars(templateVars, recordListing);
        //Then
        Assertions.assertThat(templateVars.size()).isEqualTo(10);
        Assertions.assertThat(templateVars.get(CommonConstants.CIC_CASE_RECORD_HEARING_FORMAT_VIDEO)).isEqualTo(true);
    }

    @Test
    void setRecordingTemplateVarsTest_TelephoneFormat() throws IOException {
        //Given
        RecordListing recordListing = RecordListing.builder().hearingVenueName("London Centre")
            .conferenceCallNumber("cmi459t5iut5")
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
}
