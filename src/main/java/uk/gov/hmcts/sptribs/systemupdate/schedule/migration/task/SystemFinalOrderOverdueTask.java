package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class SystemFinalOrderOverdueTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String PRONOUNCED_DATE = "coGrantedDate";

    private static final String FINAL_ORDER_OVERDUE_FLAG = "isFinalOrderOverdue";

    @Override
    public void run() {
        log.info("Final Order overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String overdueDate = formatter.format(LocalDate.now().minusMonths(12));

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(boolQuery()
                        .must(matchQuery("reference", 1688978122333564L))
                    )

                    /*.must(
                        boolQuery()
                            .should(boolQuery().must(rangeQuery(String.format(DATA, PRONOUNCED_DATE)).lt(overdueDate)))
                    )
                    .mustNot(matchQuery(String.format(DATA, FINAL_ORDER_OVERDUE_FLAG), YesOrNo.YES))*/
                ;


            final List<CaseDetails> casesInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, State.AwaitingHearing);

            for (final CaseDetails caseDetails : casesInAwaitingFinalOrderState) {
                triggerFinalOrderEventForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("Final Order overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Final Order overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Final Order overdue schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerFinalOrderEventForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Final Order Overdue Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_MIGRATE_CASE, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
