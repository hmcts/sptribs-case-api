package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CASE_FILE_VIEW;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerCaseFileView implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
            .event(CASEWORKER_CASE_FILE_VIEW)
            .forAllStates()
            .name("Case File View")
            .description("Case File View")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));

        pageBuilder.page("caseFileViewPage")
            .mandatory(CaseData::getCaseFileView1)
            .done();

    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker case file callback invoked for Case Id: {}", details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

}
