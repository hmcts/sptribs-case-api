package uk.gov.hmcts.sptribs.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.io.Writer;
import java.sql.Connection;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
    void shouldPopulateTsvOnAboutToStart() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        caseDetails.setData(caseData);

        DataSource dataSource = org.mockito.Mockito.mock(DataSource.class);
        Connection connection = org.mockito.Mockito.mock(Connection.class);
        PGConnection pgConnection = org.mockito.Mockito.mock(PGConnection.class);
        CopyManager copyManager = org.mockito.Mockito.mock(CopyManager.class);

        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.unwrap(PGConnection.class)).thenReturn(pgConnection);
        when(pgConnection.getCopyAPI()).thenReturn(copyManager);

        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(1);
            writer.write(
                "Case reference\tCreated (UTC)\tState\tState since (UTC)\tClosure reason\tClosure details\n"
                    + "123\t2025-12-18 10:00\tCaseClosed\t2025-12-17 10:00\tcaseRejected\t"
                    + "deadlineMissed\n"
            );
            return 1L;
        }).when(copyManager).copyOut(anyString(), any(Writer.class));

        AboutToStartOrSubmitResponse<CaseData, State> response = event.aboutToStart(caseDetails);

        String tsv = response.getData().getOpenCasesStatisticsTsv();
        assertThat(tsv).contains("Case reference\tCreated (UTC)\tState\tState since (UTC)");
        assertThat(tsv).contains("123\t");
        assertThat(tsv).contains("\tcaseRejected\t");
        assertThat(tsv).contains("deadlineMissed");
    }
}
