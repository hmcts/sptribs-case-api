package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementAmendDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.DocumentManagementSelectDocuments;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import static uk.gov.hmcts.sptribs.caseworker.util.CaseDocumentListUtil.updateCaseDocumentList;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_AMEND;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CLOSE_CASE_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOC_MGMT_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.HEARING_SUMMARY_TYPE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.REINSTATE_TYPE;

@Component
@Slf4j
@Setter
public class CaseworkerDocumentManagementAmend implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration selectDocuments = new DocumentManagementSelectDocuments();
    private static final CcdPageConfiguration amendDocuments = new DocumentManagementAmendDocuments();

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_DOCUMENT_MANAGEMENT_AMEND)
                .forStates(Withdrawn,
                    Rejected,
                    Submitted,
                    NewCaseReceived,
                    CaseManagement,
                    ReadyToList,
                    AwaitingHearing,
                    AwaitingOutcome,
                    CaseClosed,
                    CaseStayed)
                .name("Document management: Amend")
                .description("Document management: Amend")
                .showSummary()
                .grant(CREATE_READ_UPDATE_DELETE, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_TEAM_LEADER)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_SENIOR_JUDGE, ST_CIC_CASEWORKER,
                    ST_CIC_HEARING_CENTRE_ADMIN)
                .grantHistoryOnly(ST_CIC_JUDGE)
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        selectDocuments.addTo(pageBuilder);
        amendDocuments.addTo(pageBuilder);
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

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();

        final DocumentType selectedDocumentCategory = cicCase.getSelectedDocumentCategory();
        final String selectedDocumentEmailContent = cicCase.getSelectedDocumentEmailContent();
        final Document selectedDocumentLink = cicCase.getSelectedDocumentLink();
        final String selectedDocumentType = cicCase.getSelectedDocumentType();

        switch (selectedDocumentType) {
            case CASE_TYPE:
                updateCaseDocumentList(
                    cicCase.getApplicantDocumentsUploaded(),
                    selectedDocumentCategory,
                    selectedDocumentEmailContent,
                    selectedDocumentLink
                );
                break;
            case REINSTATE_TYPE:
                updateCaseDocumentList(
                    cicCase.getReinstateDocuments(),
                    selectedDocumentCategory,
                    selectedDocumentEmailContent,
                    selectedDocumentLink
                );
                break;
            case DOC_MGMT_TYPE:
                updateCaseDocumentList(
                    data.getAllDocManagement().getCaseworkerCICDocument(),
                    selectedDocumentCategory,
                    selectedDocumentEmailContent,
                    selectedDocumentLink
                );
                break;
            case CLOSE_CASE_TYPE:
                updateCaseDocumentList(
                    data.getCloseCase().getDocuments(),
                    selectedDocumentCategory,
                    selectedDocumentEmailContent,
                    selectedDocumentLink
                );
                break;
            case HEARING_SUMMARY_TYPE:
                updateCaseDocumentList(
                    data.getLatestCompletedHearing().getSummary().getRecFile(),
                    selectedDocumentCategory,
                    selectedDocumentEmailContent,
                    selectedDocumentLink
                );
                break;
            default:
                break;
        }
        cicCase.setSelectedDocumentCategory(null);
        cicCase.setSelectedDocumentEmailContent(null);
        cicCase.setSelectedDocumentLink(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();

    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Document Updated")
            .build();
    }

}
