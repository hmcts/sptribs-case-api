package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.ciccase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.ciccase.model.State.FinalOrderPending;
import static uk.gov.hmcts.divorce.ciccase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerFinalOrderPending implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ADD_ADMIN_CLARIFICATION = "caseworker-final-order-pending";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ADD_ADMIN_CLARIFICATION)
            .forStateTransition(EnumSet.of(FinalOrderRequested, AwaitingFinalOrder, FinalOrderOverdue), FinalOrderPending)
            .name("Final Order pending")
            .description("Final Order pending")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN));
    }
}
