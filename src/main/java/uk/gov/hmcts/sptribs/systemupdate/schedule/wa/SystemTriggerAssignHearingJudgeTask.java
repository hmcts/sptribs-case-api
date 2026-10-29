package uk.gov.hmcts.sptribs.systemupdate.schedule.wa;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingJudgeTaskDateService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerAssignHearingJudge.SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE;

@Component
@Slf4j
public class SystemTriggerAssignHearingJudgeTask implements Runnable {

    private final CcdSearchService searchService;
    private final CcdUpdateService updateService;
    private final CaseDetailsConverter converter;
    private final HearingJudgeTaskDateService dates;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    public SystemTriggerAssignHearingJudgeTask(CcdSearchService searchService, CcdUpdateService updateService,
                                               CaseDetailsConverter converter, HearingJudgeTaskDateService dates,
                                               IdamService idamService, AuthTokenGenerator authTokenGenerator) {
        this.searchService = searchService;
        this.updateService = updateService;
        this.converter = converter;
        this.dates = dates;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public void run() {
        CICUser user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuth = authTokenGenerator.generate();
        LocalDate today = dates.today();
        BoolQueryBuilder query = boolQuery()
            .must(matchQuery("state", "AwaitingHearing"))
            .filter(rangeQuery("data.hearingList.value.date").from(today));
        try {
            for (CaseDetails caseDetails : searchService.searchForAllCasesWithQuery(query, user, serviceAuth)) {
                triggerAssignHearingJudgeEvent(user, serviceAuth, caseDetails);
            }
        } catch (CcdSearchCaseException | CcdConflictException e) {
            log.error("Unable to search for hearing judge tasks", e);
        }
    }

    private void triggerAssignHearingJudgeEvent(CICUser user, String serviceAuth, CaseDetails caseDetails) {
        try {
            CaseData data = converter.convertToCaseDetailsFromReformModel(caseDetails).getData();
            if (data.getHearingList() == null) {
                return;
            }
            for (ListValue<Listing> hearing : data.getHearingList()) {
                Listing listing = hearing.getValue();
                if (listing != null && listing.getHearingStatus() == Listed
                    && listing.getHearingJudgeTaskRequested() != YES
                    && dates.isDue(listing.getDate(), dates.releaseDate(listing.getDate()))) {
                    updateService.submitEvent(caseDetails.getId(), SYSTEM_TRIGGER_ASSIGN_HEARING_JUDGE,
                        user, serviceAuth);
                    break;
                }
            }
        } catch (CcdManagementException | IllegalArgumentException e) {
            log.error("Unable to trigger hearing judge task for case {}", caseDetails.getId(), e);
        }
    }
}
