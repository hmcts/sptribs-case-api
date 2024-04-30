package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import java.util.ArrayList;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class SystemMigrateCaseFlags implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_CASE_FLAGS = "system-migrate-case-flags";

    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Value("${feature.migration.enabled}")
    private boolean migrationFlagEnabled;


    @Autowired
    public SystemMigrateCaseFlags(CcdSupplementaryDataService ccdSupplementaryDataService) {
        this.ccdSupplementaryDataService = ccdSupplementaryDataService;
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (migrationFlagEnabled) {
            configBuilder
                .event(SYSTEM_MIGRATE_CASE_FLAGS)
                .forAllStates()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .name("Migrate case flags")
                .description("Migrate case flags for old cases")
                .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        if (migrationFlagEnabled) {
            log.info("Migrating case flags for case Id: {}", details.getId());
            CaseData data = details.getData();
            initialiseFlags(data);
            setSupplementaryData(details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private void initialiseFlags(CaseData data) {
        data.setCaseFlags(Flags.builder()
            .details(new ArrayList<>())
            .partyName(null)
            .roleOnCase(null)
            .build());

        if (data.getCicCase().getFullName() != null) {
            data.setSubjectFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getFullName())
                .roleOnCase("subject")
                .build()
            );
        }

        if (data.getCicCase().getApplicantFullName() != null) {
            data.setApplicantFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getApplicantFullName())
                .roleOnCase("applicant")
                .build()
            );
        }

        if (data.getCicCase().getRepresentativeFullName() != null) {
            data.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getRepresentativeFullName())
                .roleOnCase("Representative")
                .build()
            );
        }
    }

    private void setSupplementaryData(Long caseId) {
        try {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(caseId.toString());
        } catch (Exception exception) {
            log.error("Unable to set Supplementary data with exception : {}", exception.getMessage());
        }
    }
}
