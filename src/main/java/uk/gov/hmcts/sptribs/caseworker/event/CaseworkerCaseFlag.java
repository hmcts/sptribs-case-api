package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCaseFlag implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CASE_FLAG = "caseworker-case-flag";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CASE_FLAG)
            .forStates(POST_SUBMISSION_STATES)
            .name("Create a case flag")
            .showSummary()
            .description("Create a case flag")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }
}
