package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;

import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.ciccase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.ciccase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.ciccase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.ciccase.model.State.Draft;
import static uk.gov.hmcts.divorce.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenUpdateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE = "citizen-update-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE)
            .forStates(Draft, AwaitingApplicant1Response, ConditionalOrderDrafted, ConditionalOrderPending, AwaitingClarification)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }
}
