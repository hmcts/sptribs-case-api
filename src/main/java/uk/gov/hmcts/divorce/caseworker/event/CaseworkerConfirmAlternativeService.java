package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingAlternativeService;
import static uk.gov.hmcts.divorce.ciccase.model.State.Holding;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerConfirmAlternativeService implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE = "caseworker-confirm-alternative-service";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE)
            .forStateTransition(AwaitingAlternativeService, Holding)
            .name("Confirm alternative service")
            .description("Confirm alternative service")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                SOLICITOR,
                LEGAL_ADVISOR,
                CITIZEN));
    }
}
