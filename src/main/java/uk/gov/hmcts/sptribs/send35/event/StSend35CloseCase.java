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
 * {@code close-case} — Registered to Closed.
 *
 * <p>The end of the prototype's progression. Nothing else about the appeal changes: the
 * point is that a closed appeal still reads back in full across the tabs.
 */
@Component
public class StSend35CloseCase implements CCDConfig<StSend35CaseData, StSend35State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.event("close-case")
            .forStateTransition(StSend35State.Registered, StSend35State.Closed)
            .name("Appeal: Close appeal")
            .description("Close the appeal")
            .grant(CREATE_READ_UPDATE, SUPER_USER, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN);
    }
}
