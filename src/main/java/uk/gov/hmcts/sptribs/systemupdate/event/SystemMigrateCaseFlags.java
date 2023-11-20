package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.ArrayList;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemMigrateCaseFlags implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_CASE_FLAGS = "system-migrate-case-flags";
    //public static final String SYSTEM_MIGRATE_CASE_FLAGS = "system-migrate-old-hearing";

    @Autowired
    private HearingService hearingService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_CASE_FLAGS)
            .forAllStates()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("Migrate case flags")
            .description("Migrate case flags for old cases")
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEMUPDATE);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Migrating case flags for case Id: {}", details.getId());

        CaseData data = details.getData();
        initialiseFlags(data);

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

        data.setSubjectFlags(Flags.builder()
            .details(new ArrayList<>())
            .partyName(data.getCicCase().getFullName())
            .roleOnCase("subject")
            .build()
        );

        if (null != data.getCicCase().getApplicantFullName()) {
            data.setApplicantFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getApplicantFullName())
                .roleOnCase("applicant")
                .build()
            );
        }

        if (null != data.getCicCase().getRepresentativeFullName()) {
            data.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getRepresentativeFullName())
                .roleOnCase("Representative")
                .build()
            );
        }
    }

}
