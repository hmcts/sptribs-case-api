package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingJudgeClarification;
import static uk.gov.hmcts.divorce.ciccase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerAwaitingJudgeClarification implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_AWAITING_JUDGE_CLARIFICATION = "caseworker-awaiting-judge-clarification";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_AWAITING_JUDGE_CLARIFICATION)
            .forStateTransition(EnumSet.of(GeneralConsiderationComplete, AwaitingGeneralReferralPayment), AwaitingJudgeClarification)
            .showEventNotes()
            .name("Awaiting judge clarification")
            .description("Awaiting judge clarification")
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR));
    }
}
