package uk.gov.hmcts.sptribs.wpaf1.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

/**
 * {@code register-case} — Submitted to Registered.
 *
 * <p>WPAF1 tells the appellant the tribunal will write to confirm the case is registered,
 * and to chase if nothing arrives within 14 days. This is that step, and it is the only
 * case progression the prototype needs: it gives the case list and the tabs more than one
 * state to show.
 */
@Component
public class StWpaf1RegisterCase implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.event("register-case")
            .forStateTransition(StWpaf1State.Submitted, StWpaf1State.Registered)
            .name("Appeal: Register appeal")
            .description("Register the appeal and notify Veterans UK")
            .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN);
    }
}
