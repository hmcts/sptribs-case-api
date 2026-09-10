package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.RESPONDENT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;

/**
 * Root CCD config for the Criminal Injuries Compensation case type.
 *
 * <p>The events, tabs, search and work-basket configs are separate {@code CCDConfig} beans in
 * the packages below. They used to declare {@code CCDConfig<CaseData, …>} and be replayed
 * onto this case type by hand, via an upcast in {@link ConfigBuilderHelper}. They now
 * declare {@code CriminalInjuriesCompensationData} directly, so
 * {@code CCDDefinitionGenerator} groups them with this class and applies them itself.
 *
 * <p>That replay had to go, not just because it was redundant. The SDK groups configs by
 * their declared case-data class and writes one definition directory per group, named after
 * the group's case type. While those 71 beans declared the base {@code CaseData} they formed
 * a group of their own — one that no config called {@code caseType()} on, so its case type
 * id was the empty string and its output directory was
 * {@code new File(definitionsDir, "")}: the parent. Each group's write clears its directory
 * first, so writing that group deleted every case type already written to the parent.
 *
 * <p>Harmless while CIC was the only case type — it cleared an empty directory and wrote
 * itself into it. Once generated case types were added it silently deleted any of them
 * whose package sorted before {@code ciccase} (Spring injects beans in package order), which
 * surfaced as {@code FileNotFoundException: build/definitions/StApad1} against generated
 * Java that was entirely correct.
 */
@Component
@Slf4j
public class CriminalInjuriesCompensation implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CriminalInjuriesCompensationData, State, UserRole> configBuilder) {
        ConfigBuilderHelper.configureWithMandatoryConfig(configBuilder);

        configBuilder.caseType(
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeName(),
            CcdServiceCode.ST_CIC.getCaseType().getCaseTypeAcronym(),
            CcdServiceCode.ST_CIC.getCaseType().getDescription());

        configBuilder.jurisdiction(CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionId(),
            CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION.getJurisdictionName(), CcdServiceCode.ST_CIC.getCcdServiceDescription());

        configBuilder.omitHistoryForRoles(ST_CIC_RESPONDENT, RESPONDENT_CIC);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui uncomment this line
        //configBuilder.shutterService();
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
