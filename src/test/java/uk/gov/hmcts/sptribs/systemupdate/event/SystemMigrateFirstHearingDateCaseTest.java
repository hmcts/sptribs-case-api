package uk.gov.hmcts.sptribs.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateFirstHearingDateCase.SYSTEM_MIGRATE_FIRST_HEARING_DATE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemMigrateFirstHearingDateCaseTest {

    @InjectMocks
    private SystemMigrateFirstHearingDateCase systemFixFirstHearingDateCase;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void whenConfigure_thenShouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemFixFirstHearingDateCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_FIRST_HEARING_DATE);
    }

    @Test
    void givenCaseDataWithListedHearing_whenAboutToSubmit_thenFirstHearingDateIsRecomputed() {

        //Given
        Listing listing = Listing.builder()
            .date(LocalDate.of(2025, 2, 5))
            .hearingStatus(HearingState.Listed)
            .build();

        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);

        List<ListValue<Listing>> hearingList = new ArrayList<>();

        hearingList.add(listingListValue);

        final CaseData caseData = CaseData.builder()
            .hearingList(hearingList)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemFixFirstHearingDateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getFirstHearingDate()).isEqualTo(LocalDate.of(2025, 2, 5));
    }

    @Test
    void givenCaseDataWithNoListedHearing_whenAboutToSubmit_thenFirstHearingDateIsNull() {

        //Given
        final CaseData caseData = CaseData.builder()
            .hearingList(new ArrayList<>())
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemFixFirstHearingDateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getFirstHearingDate()).isNull();
    }

    @Test
    void givenCaseData_whenAboutToSubmit_thenCaseDataIsReturnedUnchanged() {

        //Given
        final CaseData caseData = CaseData.builder()
            .hearingList(new ArrayList<>())
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemFixFirstHearingDateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void givenCaseJsonWithStaleFirstHearingDateString_whenDeserialized_thenDoesNotThrowAndAboutToSubmitRecomputes() throws Exception {

        //Given
        String staleCaseJson = """
            {
                "firstHearingDate": "05 Feb 2025",
                "hearingList": []
            }
            """;

        //When
        final CaseData caseData = objectMapper.readValue(staleCaseJson, CaseData.class);

        //Then
        assertThat(caseData.getFirstHearingDate()).isNull();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemFixFirstHearingDateCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getFirstHearingDate()).isNull();
    }
}
