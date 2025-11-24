package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;

import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASE_FLAGS_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.AC_CASE_FLAGS_VIEWER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.DISTRICT_JUDGE_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.GS_PROFILE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.NON_RESPONDENT_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.RAS_CASEWORKER_VALIDATION;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.RESPONDENT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;

@Component
@Slf4j
public class CriminalInjuriesCompensation implements CCDConfig<CriminalInjuriesCompensationData, State, UserRole> {

    @Autowired
    private List<CCDConfig<CaseData, State, UserRole>> cfgs;

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

        ConfigBuilderHelper.configure(configBuilder, cfgs);

        ConfigBuilderHelper.configureWithTestEvent(configBuilder);

        // to shutter the service within xui uncomment this line
        configBuilder.shutterService(SYSTEM_UPDATE, CASEWORKER, CREATOR, DISTRICT_JUDGE_CIC, RESPONDENT_CIC, ST_CIC_CASEWORKER,
            ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE,
            ST_CIC_RESPONDENT, AC_CASE_FLAGS_ADMIN, CITIZEN, ST_CIC_WA_CONFIG_USER, NON_RESPONDENT_USER, AC_CASE_FLAGS_VIEWER,
            RAS_CASEWORKER_VALIDATION, GS_PROFILE);
        log.info("Building definition for " + System.getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
