package uk.gov.hmcts.sptribs.wpaf1.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

/**
 * Root CCD config for the WPAF1 case type.
 *
 * <p>The only place this case type calls {@code caseType()} and {@code jurisdiction()}; both
 * come from {@link CcdServiceCode}, so registration is one enum constant there and one in
 * {@code CcdCaseType}.
 *
 * <p>WPAF1 stays in the existing {@code ST_CIC} jurisdiction. A new jurisdiction would need
 * its own CCD roles, an {@code XUI_JURISDICTIONS} entry and an IDAM profile row per user —
 * none of which the prototype needs to prove anything.
 */
@Component
public class StWpaf1CaseTypeConfig implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    private static final CcdServiceCode SERVICE_CODE = CcdServiceCode.ST_WPAF1;

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.caseType(
            SERVICE_CODE.getCaseType().getCaseTypeName(),
            SERVICE_CODE.getCaseType().getCaseTypeAcronym(),
            SERVICE_CODE.getCaseType().getDescription());

        configBuilder.jurisdiction(
            SERVICE_CODE.getJurisdiction().getJurisdictionId(),
            SERVICE_CODE.getJurisdiction().getJurisdictionName(),
            SERVICE_CODE.getCcdServiceDescription());

        // sptribs is registered decentralised, so CCD routes this case type's callbacks and
        // its data to the case API rather than storing it itself.
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        for (StWpaf1State state : StWpaf1State.values()) {
            configBuilder.grant(
                state,
                CREATE_READ_UPDATE,
                SUPER_USER,
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                CITIZEN,
                CREATOR);
        }
    }
}
