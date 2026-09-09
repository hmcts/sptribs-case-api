package uk.gov.hmcts.sptribs.send35.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

/**
 * {@code register-case} — Submitted to Registered.
 *
 * <p>SEND35 tells the appellant the tribunal aims to register the appeal, or ask for more
 * information, within 10 working days. This is that step, and it is the only case
 * progression the prototype needs: it gives the case list and the tabs more than one state
 * to show.
 */
@Component
public class StSend35RegisterCase implements CCDConfig<StSend35CaseData, StSend35State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.event("register-case")
            .forStateTransition(StSend35State.Submitted, StSend35State.Registered)
            .name("Appeal: Register appeal")
            .description("Register the appeal and notify the local authority")
            .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN);
    }
}
