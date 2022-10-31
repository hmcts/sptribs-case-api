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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseNote;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerAddNote.CASEWORKER_ADD_NOTE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerAddNoteTest {

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerAddNote caseworkerAddNote;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerAddNote.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ADD_NOTE);
    }

    @Test
    public void shouldSuccessfullyAddCaseNoteToCaseDataWhenThereAreNoExistingCaseNotes() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDate = LocalDate.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAddNote.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        //Then
        assertThat(response.getData().getNotes())
            .extracting("id", "value.author", "value.note")
            .contains(tuple("1", "testFname testSname", "This is a test note"));

        assertThat(response.getData().getNotes())
            .extracting("value.date", LocalDate.class)
            .allMatch(localDate -> localDate.isEqual(expectedDate));

        assertThat(response.getData().getNote()).isNull();

        verify(httpServletRequest).getHeader(AUTHORIZATION);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        verifyNoMoreInteractions(httpServletRequest, idamService);
    }

    @Test
    public void shouldSuccessfullyAddCaseNoteToStartOfCaseNotesListWhenThereIsExistingCaseNote() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note 2");

        var caseNoteAddedDate = LocalDate.of(2021, 1, 1);

        var notes = new ArrayList<ListValue<CaseNote>>();
        notes.add(ListValue
            .<CaseNote>builder()
            .id("1")
            .value(new CaseNote("TestFirstName TestSurname", caseNoteAddedDate, "This is a test note 1"))
            .build());

        caseData.setNotes(notes);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDate = LocalDate.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(TestDataHelper.getUser());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAddNote.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        //Then
        assertThat(response.getData().getNotes())
            .extracting("id", "value.author", "value.note")
            .containsExactly(
                tuple("1", "testFname testSname", "This is a test note 2"),
                tuple("2", "TestFirstName TestSurname", "This is a test note 1")

            );

        assertThat(response.getData().getNotes())
            .extracting("value.date", LocalDate.class)
            .containsExactlyInAnyOrder(expectedDate, caseNoteAddedDate);

        assertThat(response.getData().getNote()).isNull();

        verify(httpServletRequest).getHeader(AUTHORIZATION);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        verifyNoMoreInteractions(httpServletRequest, idamService);
    }

}
