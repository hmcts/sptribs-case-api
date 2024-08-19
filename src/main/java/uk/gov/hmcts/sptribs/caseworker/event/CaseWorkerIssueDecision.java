package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionMainContent;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionPreviewTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueDecisionUploadNotice;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.DecisionIssuedNotification;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DecisionTemplateContent;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_DECISION;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DECISION_FILE;

@Component
@Slf4j
public class CaseWorkerIssueDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final CcdPageConfiguration issueDecisionNotice = new IssueDecisionNotice();
    private static final CcdPageConfiguration issueDecisionSelectTemplate = new IssueDecisionSelectTemplate();
    private static final CcdPageConfiguration issueDecisionPreviewTemplate = new IssueDecisionPreviewTemplate();
    private static final CcdPageConfiguration issueDecisionUploadNotice = new IssueDecisionUploadNotice();
    private static final CcdPageConfiguration issueDecisionSelectRecipients = new IssueDecisionSelectRecipients();
    private static final CcdPageConfiguration issueDecisionMainContent = new IssueDecisionMainContent();
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private DecisionTemplateContent decisionTemplateContent;

    @Autowired
    private DecisionIssuedNotification decisionIssuedNotification;

    @Autowired
    private HttpServletRequest request;

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ISSUE_DECISION)
                .forStates(AwaitingOutcome)
                .name("Decision: Issue a decision")
                .description("Decision: Issue a decision")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN,
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE);

        if (isWorkAllocationEnabled) {
            eventBuilder.publishToCamunda()
                        .grant(CREATE_READ_UPDATE, ST_CIC_WA_CONFIG_USER);
        }

        final PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        issueDecisionNotice.addTo(pageBuilder);
        issueDecisionSelectTemplate.addTo(pageBuilder);
        issueDecisionMainContent.addTo(pageBuilder);
        issueDecisionUploadNotice.addTo(pageBuilder);
        issueDecisionAddDocumentFooter(pageBuilder);
        issueDecisionPreviewTemplate.addTo(pageBuilder);
        issueDecisionSelectRecipients.addTo(pageBuilder);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        caseData.setDecisionSignature("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        CaseData caseData = details.getData();
        final CaseIssueDecision decision = caseData.getCaseIssueDecision();

        final Long caseId = details.getId();

        final String filename = DECISION_FILE + LocalDateTime.now().format(FORMATTER);

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            decisionTemplateContent.apply(caseData, caseId),
            caseId,
            decision.getIssueDecisionTemplate().getId(),
            LanguagePreference.ENGLISH,
            filename,
            request
        );

        decision.setIssueDecisionDraft(generalOrderDocument);
        caseData.setCaseIssueDecision(decision);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final CICDocument decisionDocument = caseData.getCaseIssueDecision().getDecisionDocument();

        if (null != decisionDocument && null != decisionDocument.getDocumentLink()) {
            decisionDocument.getDocumentLink().setCategoryId("TD");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseManagement)//or AwaitingHearing
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        try {
            sendIssueDecisionNotification(details.getData().getHyphenatedCaseRef(), details.getData());
        } catch (Exception notificationException) {
            log.error("Issue a decision notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Issue a decision notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Decision notice issued %n## %s",
                MessageUtil.generateSimpleMessage(details.getData().getCicCase())))
            .build();
    }

    private void issueDecisionAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionAddDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .label("LabelDocFooter",
                """
                    Decision Notice Signature

                    Confirm the Role and Surname of the person who made this decision - this will be added
                     to the bottom of the generated decision notice. E.g. 'Tribunal Judge Farrelly'
                    """)
            .pageShowConditions(issueDecisionShowConditions())
            .mandatory(CaseData::getDecisionSignature)
            .done();
    }

    private void sendIssueDecisionNotification(String caseNumber, CaseData data) {

        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())) {
            decisionIssuedNotification.sendToSubject(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRespondent())) {
            decisionIssuedNotification.sendToRespondent(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())) {
            decisionIssuedNotification.sendToRepresentative(data, caseNumber);
        }
        if (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant())) {
            decisionIssuedNotification.sendToApplicant(data, caseNumber);
        }
    }
}
