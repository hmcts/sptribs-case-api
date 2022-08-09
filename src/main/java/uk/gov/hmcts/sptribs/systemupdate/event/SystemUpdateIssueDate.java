package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemUpdateIssueDate implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_SYSTEM_USER_UPDATE_ISSUE_DATE = "system-update-issue-date";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_SYSTEM_USER_UPDATE_ISSUE_DATE)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("Update issue date")
            .description("Update issue date")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE))
            .page("updateIssueDate")
            .complex(CaseData::getApplication)
                .mandatory(Application::getIssueDate)
                .done();
    }
}
