package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ShowCaseDocuments;
import uk.gov.hmcts.sptribs.caseworker.event.page.ShowRemovedCaseDocuments;
import uk.gov.hmcts.sptribs.caseworker.util.DecisionDocumentListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;
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
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@Setter
public class CaseworkerDocumentManagementRemove implements CCDConfig<CaseData, State, UserRole> {

    private final ShowCaseDocuments showCaseDocuments = new ShowCaseDocuments();
    private final ShowRemovedCaseDocuments showRemovedCaseDocuments = new ShowRemovedCaseDocuments();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
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
            .name("Document management: Remove")
            .description("Document management: Remove")
            .grant(Permission.C, ST_CIC_CASEWORKER)
                .grant(Permission.D, ST_CIC_CASEWORKER)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, ST_CIC_SENIOR_JUDGE, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_TEAM_LEADER)
            .grantHistoryOnly(ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_JUDGE)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted));

        showCaseDocuments.addTo(pageBuilder);
        showRemovedCaseDocuments.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();
        final CicCase cicCase = caseData.getCicCase();

        cicCase.setFinalDecisionDocumentList(DocumentListUtil.getAllFinalDecisionDocuments(caseData));
        cicCase.setDecisionDocumentList(DocumentListUtil.getAllDecisionDocuments(caseData));
        cicCase.setOrderDocumentList(DocumentListUtil.getAllOrderDocuments(caseData.getCicCase()));
        caseData.setCicCase(cicCase);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        if (!ObjectUtils.isEmpty(caseData.getCicCase().getRemovedDocumentList())) {
            removeCaseDocuments(caseData);
        }
        List<ListValue<CaseworkerCICDocument>> listValues = new ArrayList<>();
        caseData.getCicCase().setRemovedDocumentList(listValues);
        caseData.getCicCase().setReadOnlyRemovedDocList(caseData.getCicCase().getRemovedDocumentList());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Case Updated")
            .build();
    }

    private void removeCaseDocuments(CaseData data) {
        List<ListValue<CaseworkerCICDocument>> removedDocumentList = data.getCicCase().getRemovedDocumentList();
        removedDocumentList.forEach(v -> {
            DecisionDocumentListUtil.removeFinalDecisionDraftAndCICDocument(data, v);
            DecisionDocumentListUtil.removeDecisionDraftAndCICDocument(data, v);
        });
    }

}
