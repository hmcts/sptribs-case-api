package uk.gov.hmcts.sptribs.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUPERUSER_VIEW_OPEN_CASES_STATISTICS;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
public class ViewOpenCasesStatisticsEvent implements CCDConfig<CaseData, State, UserRole> {

    private static final String STATE_CLOSED = "CaseClosed";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int SQL_LIMIT = 10_000;

    private static final String OPEN_CASES_STATISTICS_SQL = """
        select
          reference,
          created_date,
          state,
          coalesce(last_state_modified_date, created_date) as state_since,
          data ->> 'closeCloseCaseReason' as close_case_reason,
          data ->> 'closeRejectionReason' as close_rejection_reason,
          data ->> 'closeStrikeOutReason' as close_strike_out_reason,
          data ->> 'caseIssueFinalDecisionDecisionTemplate' as final_decision_template,
          data ->> 'closeAdditionalDetail' as close_additional_detail,
          data ->> 'closeRejectionDetails' as close_rejection_details,
          data ->> 'closeStrikeOutDetails' as close_strike_out_details
        from ccd.case_data
        where case_type_id = ?
        order by state_since desc
        limit ?
        """;

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

    private String generateCasesStatisticsTsv() {
        OffsetDateTime generatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        List<CaseStatisticsRow> rows = fetchCases();

        StringBuilder tsv = new StringBuilder();
        tsv.append(
            "Case reference\tCreated (UTC)\tState\tState since (UTC)\tDays in state\tTime in state"
                + "\tClosure reason\tClosure details\n"
        );

        for (CaseStatisticsRow row : rows) {
            Duration duration = Duration.between(row.stateSince().atOffset(ZoneOffset.UTC), generatedAt);
            ClosureInfo closureInfo = buildClosureInfo(row);

            tsv.append(row.reference()).append('\t')
                .append(row.createdDate().format(DATE_TIME_FORMAT)).append('\t')
                .append(row.state()).append('\t')
                .append(row.stateSince().format(DATE_TIME_FORMAT)).append('\t')
                .append(duration.toDays()).append('\t')
                .append(formatDuration(duration))
                .append('\t')
                .append(escapeTsv(closureInfo.reason()))
                .append('\t')
                .append(escapeTsv(closureInfo.details()))
                .append('\n');
        }

        return tsv.toString();
    }

    private List<CaseStatisticsRow> fetchCases() {
        String caseType = CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName();
        return jdbcTemplate.query(
            OPEN_CASES_STATISTICS_SQL,
            new Object[] {caseType, SQL_LIMIT},
            new DataClassRowMapper<>(CaseStatisticsRow.class)
        );
    }

    private ClosureInfo buildClosureInfo(CaseStatisticsRow row) {
        if (!STATE_CLOSED.equals(row.state())) {
            return new ClosureInfo("", "");
        }

        String closeReason = firstNonBlank(row.closeCaseReason(), row.finalDecisionTemplate());
        if (StringUtils.hasText(closeReason)) {
            return new ClosureInfo(closeReason, buildCloseCaseDetails(closeReason, row));
        }

        return new ClosureInfo("", "");
    }

    private String buildCloseCaseDetails(String closeReason, CaseStatisticsRow row) {
        StringBuilder details = new StringBuilder(64);

        if ("caseRejected".equals(closeReason)) {
            appendWithSeparator(details, row.closeRejectionReason());
            appendWithSeparator(details, row.closeRejectionDetails());
        } else if ("caseStrikeOut".equals(closeReason)) {
            appendWithSeparator(details, row.closeStrikeOutReason());
            appendWithSeparator(details, row.closeStrikeOutDetails());
        }

        appendWithSeparator(details, row.closeAdditionalDetail());

        return details.toString();
    }

    private void appendWithSeparator(StringBuilder sb, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" - ");
        }
        sb.append(value);
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(second)) {
            return second;
        }
        return "";
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        Duration remaining = duration.minusDays(days);
        long hours = remaining.toHours();
        long minutes = remaining.minusHours(hours).toMinutes();
        return "%dd %dh %dm".formatted(days, hours, minutes);
    }

    private String escapeTsv(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public record CaseStatisticsRow(
        long reference,
        LocalDateTime createdDate,
        String state,
        LocalDateTime stateSince,
        String closeCaseReason,
        String closeRejectionReason,
        String closeStrikeOutReason,
        String finalDecisionTemplate,
        String closeAdditionalDetail,
        String closeRejectionDetails,
        String closeStrikeOutDetails
    ) {
    }

    private record ClosureInfo(String reason, String details) {
    }
}
