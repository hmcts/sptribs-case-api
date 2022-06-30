package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.ciccase.model.State.Submitted;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_PAYMENT_MADE = "caseworker-payment-made";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PAYMENT_MADE)
            .forStateTransition(EnumSet.of(AwaitingPayment, AwaitingHWFDecision, AwaitingDocuments),
                Submitted)
            .name("Payment made")
            .description("Payment made")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR));
    }
}
