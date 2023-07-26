package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.UploadCaseDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CIC_SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.uploadDocument;

@Component
@Slf4j
public class RespondentDocumentManagement implements CCDConfig<CaseData, State, UserRole> {

    private final UploadCaseDocuments uploadCaseDocuments = new UploadCaseDocuments();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(RESPONDENT_DOCUMENT_MANAGEMENT)
            .forStates(Withdrawn,
                Rejected,
                Submitted,
                NewCaseReceived,
                CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Document management: Upload")
            .description("Document management: Upload")
            .showSummary()
            .grantHistoryOnly(CIC_SUPER_USER,
                CIC_CASEWORKER,
                CIC_SENIOR_CASEWORKER,
                CIC_CENTRE_ADMIN,
                CIC_CENTRE_TEAM_LEADER,
                CIC_SENIOR_JUDGE,
                CIC_JUDGE,
                CIC_RESPONDENT)
            .grant(CREATE_READ_UPDATE, CIC_SUPER_USER, CIC_RESPONDENT)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted));

        uploadCaseDocuments.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var data = details.getData();
        uploadDocument(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();

    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

}
