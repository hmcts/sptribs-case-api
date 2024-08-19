package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateGlobalSearchFields.SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS;

@Component
@Slf4j
public class SystemMigrateGlobalSearchTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    @Value("${feature.migrate-global-search-task.enabled}")
    private boolean globalSearchMigrationEnabled;

    @Value("${feature.migrate-global-search-task.caseReference}")
    private String globalSearchTestCaseReference;

    @Autowired
    public SystemMigrateGlobalSearchTask(AuthTokenGenerator authTokenGenerator, CcdSearchService ccdSearchService,
                                         CcdUpdateService ccdUpdateService, IdamService idamService) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
    }

    @Override
    public void run() {
        if (globalSearchMigrationEnabled) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            log.info("User name: " + user.getUserDetails().getEmail());
            log.info("User roles: " + String.join(",", user.getUserDetails().getRoles()));

            try {
                final BoolQueryBuilder query =
                    boolQuery()
                        .must(boolQuery()
                            .mustNot(existsQuery("data.SearchCriteria"))
                        );

                if (globalSearchTestCaseReference != null && !globalSearchTestCaseReference.trim().equals("")) {
                    query.must(matchQuery("reference", Long.parseLong(globalSearchTestCaseReference)));
                }

                final List<CaseDetails> casesToMigrateSearchCriteria =
                    ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
                log.info("Cases:" + casesToMigrateSearchCriteria.size());

                for (final CaseDetails caseDetails : casesToMigrateSearchCriteria) {
                    triggerSystemMigrateSearchCriteriaTask(user, serviceAuth, caseDetails);
                }

                log.info("System migrate search criteria scheduled task complete.");
            } catch (final CcdSearchCaseException e) {
                log.error("System migrate search criteria schedule task stopped after search error", e);
            } catch (final CcdConflictException e) {
                log.info("System migrate search criteria schedule task stopping "
                    + "due to conflict with another running task"
                );
            }
        }
    }

    private void triggerSystemMigrateSearchCriteriaTask(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting System Migrate Search Criteria Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_MIGRATE_GLOBAL_SEARCH_FIELDS, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
