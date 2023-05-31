package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueCaseSelectDocument;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseIssuedNotification;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.DELETE;

@Component
@Slf4j
public class CaseworkerIssueCase implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration issueCaseNotifyParties = new IssueCaseNotifyParties();
    private static final CcdPageConfiguration issueCaseSelectDocument = new IssueCaseSelectDocument();

    @Autowired
    private CaseIssuedNotification caseIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_CASE)
            .forStates(CaseManagement)
            .name("Case: Issue to respondent")
            .description("Case: Issue to respondent")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::issued)
            .grant(DELETE,
                ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE));
        issueCaseSelectDocument.addTo(pageBuilder);
        issueCaseNotifyParties.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();

        DynamicMultiSelectList documentList = DocumentListUtil.prepareDocumentList(caseData);
        caseData.getCaseIssue().setDocumentList(documentList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse issued(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var cicCase = data.getCicCase();
        String caseNumber = data.getHyphenatedCaseRef();

        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            caseIssuedNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
            caseIssuedNotification.sendToApplicant(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            caseIssuedNotification.sendToRepresentative(details.getData(), caseNumber);
        }

        caseIssuedNotification.sendToRespondent(details.getData(), caseNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case issued %n##  This case has now been issued. %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }
}
