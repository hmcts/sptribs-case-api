package uk.gov.hmcts.sptribs.common.event;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.postgresql.PGConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.io.StringWriter;
import java.sql.Connection;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUPERUSER_VIEW_OPEN_CASES_STATISTICS;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
public class ViewOpenCasesStatisticsEvent implements CCDConfig<CaseData, State, UserRole> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(SUPERUSER_VIEW_OPEN_CASES_STATISTICS)
            .forAllStates()
            .name("View case statistics")
            .description("Shows a tab-separated report for copy/paste into Excel (includes closure reasons for CaseClosed)")
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER));

        pageBuilder.page("viewOpenCasesStatistics")
            .pageLabel("View case statistics")
            .label(
                "viewOpenCasesStatisticsLabel",
                "Copy the report below into Excel (Ctrl/Cmd+A then Ctrl/Cmd+C). Closed cases include closure columns."
            )
            .optional(CaseData::getOpenCasesStatisticsTsv)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(
        CaseDetails<CaseData, State> details
    ) {
        CaseData data = details.getData();
        data.setOpenCasesStatisticsTsv(generateCasesStatisticsTsv());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    @SneakyThrows
    private String generateCasesStatisticsTsv() {
        String copySql = """
            COPY (
              select
                reference::text                                   as "Case reference",
                to_char(created_date, 'YYYY-MM-DD HH24:MI')       as "Created (UTC)",
                state                                             as "State",
                to_char(coalesce(last_state_modified_date, created_date), 'YYYY-MM-DD HH24:MI')
                                                                  as "State since (UTC)",
                coalesce(data ->> 'closeCloseCaseReason', '')     as "Closure reason",
                concat_ws(' - ',
                  nullif(data ->> 'closeRejectionReason', ''),
                  nullif(data ->> 'closeStrikeOutReason', ''),
                  nullif(data ->> 'caseIssueFinalDecisionDecisionTemplate', '')
                )                                                 as "Closure details"
              from ccd.case_data
              where case_type_id = 'CriminalInjuriesCompensation'
              limit 50000
            ) TO STDOUT WITH (FORMAT csv, DELIMITER E'\\t', HEADER TRUE, NULL '');
            """;

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            StringWriter writer = new StringWriter(256 * 1024);
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            pgConnection.getCopyAPI().copyOut(copySql, writer);
            return writer.toString();
        }
    }
}
