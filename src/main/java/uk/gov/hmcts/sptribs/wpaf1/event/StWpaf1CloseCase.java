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
 * {@code close-case} — Registered to Closed.
 *
 * <p>The end of the prototype's progression. Nothing else about the appeal changes: the
 * point is that a closed appeal still reads back in full across the tabs.
 */
@Component
public class StWpaf1CloseCase implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.event("close-case")
            .forStateTransition(StWpaf1State.Registered, StWpaf1State.Closed)
            .name("Appeal: Close appeal")
            .description("Close the appeal")
            .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN);
    }
}
