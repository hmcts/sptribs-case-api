package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementAmendDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementSelectDocuments;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import static uk.gov.hmcts.sptribs.caseworker.util.CaseDocumentListUtil.updateCaseDocumentList;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_AMEND;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CLOSE_CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOC_MGMT_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.HEARING_SUMMARY_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.REINSTATE_TYPE;

@Component
@Slf4j
@Setter
public class CaseworkerDocumentManagementAmend implements CCDConfig<CaseData, State, UserRole> {

    @Value("${feature.case-file-view-and-document-management.enabled}")
    private boolean caseFileViewAndDocumentManagementEnabled;

    private static final CcdPageConfiguration selectDocuments = new DocumentManagementSelectDocuments();
    private static final CcdPageConfiguration amendDocuments = new DocumentManagementAmendDocuments();

    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        if (caseFileViewAndDocumentManagementEnabled) {
            doConfigure(configBuilder);
        }
    }

    public void doConfigure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND)
            .forStates(Withdrawn,
                Rejected,
                Submitted,
                NewCaseReceived,
                CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Document management: Amend")
            .description("Document management: Amend")
            .showSummary()
            .grant(CREATE_READ_UPDATE, SUPER_USER,
                ST_CIC_SENIOR_JUDGE, ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER)
            .grantHistoryOnly(
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER,
                ST_CIC_SENIOR_JUDGE,
                SUPER_USER,
                ST_CIC_JUDGE)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted));

        selectDocuments.addTo(pageBuilder);
        amendDocuments.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var data = details.getData();
        var cicCase = data.getCicCase();

        CaseworkerCICDocument selectedDocument = cicCase.getSelectedDocument();
        String selectedDocumentType = cicCase.getSelectedDocumentType();

        switch (selectedDocumentType) {
            case CASE_TYPE:
                updateCaseDocumentList(cicCase.getApplicantDocumentsUploaded(), selectedDocument);
                break;
            case REINSTATE_TYPE:
                updateCaseDocumentList(cicCase.getReinstateDocuments(), selectedDocument);
                break;
            case DOC_MGMT_TYPE:
                updateCaseDocumentList(data.getAllDocManagement().getCaseworkerCICDocument(), selectedDocument);
                break;
            case CLOSE_CASE_TYPE:
                updateCaseDocumentList(data.getCloseCase().getDocuments(), selectedDocument);
                break;
            case HEARING_SUMMARY_TYPE:
                updateCaseDocumentList(data.getListing().getSummary().getRecFile(), selectedDocument);
                break;
            default:
                break;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();

        DynamicList documentList = DocumentListUtil.prepareCICDocumentListWithAllDocuments(caseData);
        cicCase.setAmendDocumentList(documentList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# document Updated")
            .build();
    }

}
