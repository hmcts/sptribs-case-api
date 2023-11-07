package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemMigrateCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_CASE = "system-migrate-case";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
                .event(SYSTEM_MIGRATE_CASE)
                .forAllStates()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .name("Migrate case data")
                .description("Migrate case data to the latest version")
                .grant(CREATE_READ_UPDATE_DELETE, SYSTEMUPDATE));

        //addFields(pageBuilder);

    }

    private void addFields(PageBuilder pageBuilder) {
        pageBuilder.page("retiredFields")
                .pageLabel("Retired Fields")
                .label("RetiredFields", "")
                .complex(CaseData::getRetiredFields)
                .readonly(RetiredFields::getCicBundles)
                .readonly(RetiredFields::getCicCaseHearingCancellationReason)
                .readonly(RetiredFields::getCicCaseCancelHearingAdditionalDetail)
                .readonly(RetiredFields::getCicCasePostponeReason)
                .readonly(RetiredFields::getCicCasePostponeAdditionalInformation)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Migrating case data for case Id: {}", details.getId());

        CaseData data = details.getData();
        data.setCaseBundles(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .build();
    }
}
