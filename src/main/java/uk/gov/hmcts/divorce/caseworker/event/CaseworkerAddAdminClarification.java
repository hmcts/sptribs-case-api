package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.ciccase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerAddAdminClarification implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ADD_ADMIN_CLARIFICATION = "caseworker-add-admin-clarification";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ADD_ADMIN_CLARIFICATION)
            .forStateTransition(AwaitingAdminClarification, ClarificationSubmitted)
            .name("Add admin clarification")
            .description("Add admin clarification")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN));
    }
}
