package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionUploadNotice;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.DecisionIssuedNotification;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.COMMA_SPACE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.common.CommonConstants.RESPONDENT;
import static uk.gov.hmcts.sptribs.common.CommonConstants.SUBJECT;

@Component
@Slf4j
public class CaseWorkerIssueDecision implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ISSUE_DECISION = "caseworker-issue-decision";

    private static final CcdPageConfiguration issueDecisionNotice = new IssueDecisionNotice();
    private static final CcdPageConfiguration issueDecisionSelectTemplate = new IssueDecisionSelectTemplate();
    private static final CcdPageConfiguration issueDecisionUploadNotice = new IssueDecisionUploadNotice();
    private static final CcdPageConfiguration issueDecisionSelectRecipients = new IssueDecisionSelectRecipients();

    @Autowired
    private DecisionIssuedNotification decisionIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_DECISION)
            .forStates(AwaitingOutcome)
            .name("Decision: Issue a decision")
            .description("Decision: Issue a decision")
            .showEventNotes()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueDecisionNotice.addTo(pageBuilder);
        issueDecisionSelectTemplate.addTo(pageBuilder);
        issueDecisionUploadNotice.addTo(pageBuilder);
        issueDecisionSelectRecipients.addTo(pageBuilder);
        issueDecisionAddDocumentFooter(pageBuilder);
    }

    private void issueDecisionAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("addDocumentFooter")
            .pageLabel("Document footer")
            .label("LabelDocFooter",
                """

                    Decision Notice Signature
                    Confirm the Role and Surname of the person who made this decision - this will be added"
                                        + " to the bottom of the generated decision notice. E.g. 'Tribunal Judge Farrelly'
                    """)
            .mandatory(CaseData::getDecisionSignature)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)//or AwaitingHearing
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        sendIssueDecisionNotification(details.getData().getHyphenatedCaseRef(), details.getData());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Decision notice issued %n## %s",
                generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }

    private void sendIssueDecisionNotification(String caseNumber, CaseData data) {

        if (!CollectionUtils.isEmpty(data.getCicCase().getIssueDecNotifyPartySubject())) {
            decisionIssuedNotification.sendToSubject(data, caseNumber);
        }

        if (!CollectionUtils.isEmpty(data.getCicCase().getIssueDecNotifyPartyRespondent())) {
            decisionIssuedNotification.sendToRespondent(data, caseNumber);
        }

        if (!CollectionUtils.isEmpty(data.getCicCase().getIssueDecNotifyPartyRepresentative())) {
            decisionIssuedNotification.sendToRepresentative(data, caseNumber);
        }
    }

    private static String generateSimpleMessage(final CicCase cicCase) {
        final StringBuilder message = new StringBuilder(100);
        message.append("A notification has been sent to: ");

        if (!CollectionUtils.isEmpty(cicCase.getIssueDecNotifyPartySubject())) {
            message.append(SUBJECT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getIssueDecNotifyPartyRespondent())) {
            message.append(RESPONDENT + COMMA_SPACE);
        }
        if (!CollectionUtils.isEmpty(cicCase.getIssueDecNotifyPartyRepresentative())) {
            message.append(REPRESENTATIVE + COMMA_SPACE);
        }

        return message.substring(0, message.length() - 2);
    }
}
