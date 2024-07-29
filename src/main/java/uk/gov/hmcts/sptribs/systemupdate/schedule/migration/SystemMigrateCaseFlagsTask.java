package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

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
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseFlags.SYSTEM_MIGRATE_CASE_FLAGS;

@Component
@Slf4j

public class SystemMigrateCaseFlagsTask implements Runnable {

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SystemMigrateCaseFlagsTask(CcdSearchService ccdSearchService, CcdUpdateService ccdUpdateService,
            IdamService idamService, AuthTokenGenerator authTokenGenerator) {
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public void run() {
        log.info("Migrate case flags scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .mustNot(existsQuery("data.subjectFlags"))
                .mustNot(matchQuery("state", "DSS_Draft"));

            final List<CaseDetails> casesNeedsCaseFlagsMigration =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
            log.info("Cases:" + casesNeedsCaseFlagsMigration.size());
            for (final CaseDetails caseDetails : casesNeedsCaseFlagsMigration) {
                triggerMigrateFieldsForEligibleCases(user, serviceAuth, caseDetails);
            }

            log.info("Migrate fields scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Migrate fields schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Migrate fields schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerMigrateFieldsForEligibleCases(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Migrate Fields Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_MIGRATE_CASE_FLAGS, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
