package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseLinks.SYSTEM_MIGRATE_CASE_LINKS;

@Component
@Slf4j

public class SystemMigrateCaseLinksTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("Migrate case links scheduled task started");

        final User userDetails = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorisation = authTokenGenerator.generate();

        try {
            migrateCases(userDetails, serviceAuthorisation);
            log.info("Migrate Case Links task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Migrate Case Links task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Migrate Case Links task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void migrateCases(User userDetails, String serviceAuthorisation) {
        final BoolQueryBuilder boolQueryBuilder =
            boolQuery()
                .must(boolQuery()
                    .must(matchQuery("reference", 1699100726058557L))
                );
        log.info("Query:" + boolQueryBuilder.toString());

        final List<CaseDetails> caseList =
            ccdSearchService.searchForAllCasesWithQuery(boolQueryBuilder, userDetails, serviceAuthorisation);
        log.info("Cases:" + caseList.size());
        for (final CaseDetails caseDetails : caseList) {
            triggerMigrateForOldCases(userDetails, serviceAuthorisation, caseDetails);
        }
    }

    private void triggerMigrateForOldCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Case Links Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_MIGRATE_CASE_LINKS, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
