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
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseLinks.SYSTEM_MIGRATE_CASE_LINKS;

@Component
@Slf4j
public class SystemMigrateCaseLinksTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    @Autowired
    public SystemMigrateCaseLinksTask(AuthTokenGenerator authTokenGenerator, CcdSearchService ccdSearchService,
            CcdUpdateService ccdUpdateService, IdamService idamService) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
    }

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
                    .mustNot(existsQuery("data.caseNameHmctsInternal"))
                );
        log.info("Query:" + boolQueryBuilder.toString());

        final List<CaseDetails> caseList =
            ccdSearchService.searchForAllCasesWithQuery(boolQueryBuilder, userDetails, serviceAuthorisation);
        log.info("Cases:" + caseList.size());
        for (final CaseDetails caseDetails : caseList) {
            triggerMigrateForOldCases(SYSTEM_MIGRATE_CASE_LINKS, userDetails, serviceAuthorisation, caseDetails.getId());
        }
    }

    private void triggerMigrateForOldCases(String eventName, User user, String serviceAuth, Long caseId) {
        try {
            log.info("Submitting Case Links Event for Case {}", caseId);
            ccdUpdateService.submitEvent(caseId, eventName, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
