package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ShowCaseDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.service.DocumentListService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerDocumentManagementRemove implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    DocumentListService documentListService;

    private final ShowCaseDocuments showCaseDocuments = new ShowCaseDocuments();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
            .forStates(Withdrawn,
                Rejected,
                Submitted,
                NewCaseReceived,
                CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Document Management: Remove")
            .description("Document Management: Remove")
            .showSummary()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted));

        showCaseDocuments.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        caseData.getCicCase().setFinalDecisionDocumentList(documentListService.getAllFinalDecisionDocuments(caseData));
        caseData.getCicCase().setDecisionDocumentList(documentListService.getAllDecisionDocuments(caseData));
        caseData.getCicCase().setOrderDocumentList(documentListService.getAllOrderDocuments(caseData.getCicCase()));
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        var newCaseData = removeEvaluatedListDoc(caseData);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(newCaseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

    private CaseData removeFinalDecisionDoc(CaseData caseData) {
        List<ListValue<CaseworkerCICDocument>> wholeFinalDecisionDocList = documentListService.getAllFinalDecisionDocuments(caseData);
        if (wholeFinalDecisionDocList.size() > caseData.getCicCase().getFinalDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeFinalDecisionDocList) {
                if (!caseData.getCicCase().getFinalDecisionDocumentList().contains(cicDocumentListValue)) {
                    if (cicDocumentListValue.getValue().getDocumentLink()
                        .equals(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft())) {
                        caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(null);
                    } else if (cicDocumentListValue.getValue().getDocumentLink()
                        .equals(caseData.getCaseIssueFinalDecision().getDocument().getDocumentLink())) {
                        caseData.getCaseIssueFinalDecision().setDocument(new CICDocument());
                    }
                }
            }
        }
        return caseData;
    }

    private CaseData removeDecisionDoc(CaseData caseData) {
        List<ListValue<CaseworkerCICDocument>> wholeDecisionDocList = documentListService.getAllDecisionDocuments(caseData);

        if (wholeDecisionDocList.size() > caseData.getCicCase().getDecisionDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> doc : wholeDecisionDocList) {
                if (!caseData.getCicCase().getDecisionDocumentList().contains(doc)) {
                    if (doc.getValue().getDocumentLink().equals(caseData.getCaseIssueDecision().getIssueDecisionDraft())) {
                        caseData.getCaseIssueDecision().setIssueDecisionDraft(null);
                    } else if (doc.getValue().getDocumentLink()
                        .equals(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink())) {
                        caseData.getCaseIssueDecision().setDecisionDocument(new CICDocument());
                    }
                }
            }
        }
        return caseData;
    }

    private CicCase removeOrderDoc(CicCase cicCase) {
        List<ListValue<CaseworkerCICDocument>> wholeOrderDocList = documentListService.getAllOrderDocuments(cicCase);

        if (wholeOrderDocList.size() > cicCase.getOrderDocumentList().size()) {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : wholeOrderDocList) {
                if (!cicCase.getOrderDocumentList().contains(cicDocumentListValue)) {
                    for (ListValue<Order> orderListValue : cicCase.getOrderList()) {
                        if (null != orderListValue.getValue().getDraftOrder()
                            && cicDocumentListValue.getValue().getDocumentLink()
                            .equals(orderListValue.getValue().getDraftOrder().getTemplateGeneratedDocument())) {
                            orderListValue.getValue().getDraftOrder().setTemplateGeneratedDocument(null);
                        } else {
                            if (!CollectionUtils.isEmpty(orderListValue.getValue().getUploadedFile())) {
                                for (int i = 0; i < orderListValue.getValue().getUploadedFile().size(); i++) {
                                    ListValue<CICDocument> file = orderListValue.getValue().getUploadedFile().get(i);
                                    if (null != file.getValue().getDocumentLink() && file.getValue().getDocumentLink()
                                        .equals(cicDocumentListValue.getValue().getDocumentLink())) {
                                        orderListValue.getValue().getUploadedFile().get(i).setValue(new CICDocument());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return cicCase;
    }

    private CaseData removeEvaluatedListDoc(CaseData caseData) {
        var caseDataAfterDecision = removeDecisionDoc(caseData);
        var caseDataAfterFinalDecision = removeFinalDecisionDoc(caseDataAfterDecision);
        var cic = removeOrderDoc(caseDataAfterFinalDecision.getCicCase());
        caseDataAfterFinalDecision.setCicCase(cic);
        return caseDataAfterFinalDecision;
    }

}
