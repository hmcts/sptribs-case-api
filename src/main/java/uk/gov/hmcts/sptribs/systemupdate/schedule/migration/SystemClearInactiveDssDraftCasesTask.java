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

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemClearInactiveDssDraftCase.SYSTEM_CLEAR_INACTIVE_DSS_DRAFT_CASE;

@Component
@Slf4j
public class SystemClearInactiveDssDraftCasesTask implements Runnable {

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SystemClearInactiveDssDraftCasesTask(CcdSearchService ccdSearchService,
                                                CcdUpdateService ccdUpdateService,
                                                IdamService idamService,
                                                AuthTokenGenerator authTokenGenerator) {
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public void run() {
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery("state", "DSS_Draft"))
                .filter(rangeQuery("last_modified").lte(LocalDate.now().minusDays(30)));

            final List<CaseDetails> inactiveCasesInDssDraftState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth);
            log.info("Cases:" + inactiveCasesInDssDraftState.size());
            for (final CaseDetails caseDetails : inactiveCasesInDssDraftState) {
                triggerSystemClearInactiveDssDraftCase(user, serviceAuth, caseDetails);
            }

            log.info("System clear inactive Dss Draft cases scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("System clear inactive Dss Draft cases scheduled task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("System clear inactive Dss Draft cases scheduled task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerSystemClearInactiveDssDraftCase(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("System Clear Inactive Dss Draft Cases Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_CLEAR_INACTIVE_DSS_DRAFT_CASE, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}