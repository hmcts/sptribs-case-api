package uk.gov.hmcts.sptribs.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUPERUSER_VIEW_OPEN_CASES_STATISTICS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class ViewOpenCasesStatisticsEventTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ViewOpenCasesStatisticsEvent event;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        event.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUPERUSER_VIEW_OPEN_CASES_STATISTICS);
    }

    @Test
    void shouldPopulateTsvOnAboutToStart() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        caseDetails.setData(caseData);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<ViewOpenCasesStatisticsEvent.CaseStatisticsRow> rows = List.of(
            new ViewOpenCasesStatisticsEvent.CaseStatisticsRow(
                123L,
                now.minusDays(10),
                "CaseClosed",
                now.minusDays(1),
                "caseRejected",
                "deadlineMissed",
                null,
                null,
                "additional detail",
                "rejection details",
                null
            )
        );

        when(jdbcTemplate.query(
            anyString(),
            any(Object[].class),
            org.mockito.ArgumentMatchers.<RowMapper<ViewOpenCasesStatisticsEvent.CaseStatisticsRow>>any()
        )).thenReturn(rows);

        AboutToStartOrSubmitResponse<CaseData, State> response = event.aboutToStart(caseDetails);

        String tsv = response.getData().getOpenCasesStatisticsTsv();
        assertThat(tsv).contains("Case reference\tCreated (UTC)\tState\tState since (UTC)");
        assertThat(tsv).contains("123\t");
        assertThat(tsv).contains("\tcaseRejected\t");
        assertThat(tsv).contains("deadlineMissed");
        assertThat(tsv).contains("rejection details");
        assertThat(tsv).contains("additional detail");
    }
}
