package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionMainContent;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionPreviewTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseFinalDecisionIssuedNotification;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_ANNEX_FILE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_ANNEX_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_FILE;

@Component
@Slf4j
public class CaseworkerIssueFinalDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final CcdPageConfiguration issueFinalDecisionNotice = new IssueFinalDecisionNotice();

    private static final IssueFinalDecisionSelectTemplate issueFinalDecisionSelectTemplate = new IssueFinalDecisionSelectTemplate();

    private static final CcdPageConfiguration issueFinalDecisionPreviewTemplate = new IssueFinalDecisionPreviewTemplate();

    private static final CcdPageConfiguration issueFinalDecisionSelectRecipients = new IssueFinalDecisionSelectRecipients();

    private static final CcdPageConfiguration issueFinalDecisionMainContent = new IssueFinalDecisionMainContent();

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private FinalDecisionTemplateContent finalDecisionTemplateContent;

    @Autowired
    private CaseFinalDecisionIssuedNotification caseFinalDecisionIssuedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_FINAL_DECISION)
            .forStates(AwaitingOutcome)
            .name("Decision: Issue final decision")
            .description("Decision: Issue final decision")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
        issueFinalDecisionMainContent.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        issueFinalDecisionAddDocumentFooter(pageBuilder);
        issueFinalDecisionPreviewTemplate.addTo(pageBuilder);
        issueFinalDecisionSelectRecipients.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();

        caseData.setDecisionSignature("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void uploadDocuments(PageBuilder pageBuilder) {

        pageBuilder.page("issueFinalDecisionUpload")
            .pageLabel("Upload decision notice")
            .pageShowConditions(issueFinalDecisionShowConditions())
            .label("LabelDoc", """
                Upload a copy of the decision notice that you want to add to this case.
                  *  <h3>The decision notice should be:</h3>
                  *  a maximum of 100MB in size (larger files must be split)
                  *  labelled clearly, e.g. applicant-name-decision-notice.pdf
                """
            )
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatoryWithLabel(CaseIssueFinalDecision::getDocuments, "File Attachments")
            .done();
    }

    private void issueFinalDecisionAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("issueFinalDecisionAddDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .label("LabelIssueFinalDecisionAddDocumentFooter",
                """
                    Decision Notice Signature

                    Confirm the Role and Surname of the person who made this decision - this will be added
                     to the bottom of the generated decision notice. E.g. 'Tribunal Judge Farrelly'
                    """)
            .pageShowConditions(issueFinalDecisionShowConditions())
            .mandatory(CaseData::getDecisionSignature)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        CaseData caseData = details.getData();
        var finalDecision = caseData.getCaseIssueFinalDecision();

        final Long caseId = details.getId();

        final String filename = FINAL_DECISION_FILE + LocalDateTime.now().format(formatter);

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            finalDecisionTemplateContent.apply(caseData, caseId),
            caseId,
            finalDecision.getDecisionTemplate().getId(),
            LanguagePreference.ENGLISH,
            filename,
            request
        );

        finalDecision.setFinalDecisionDraft(generalOrderDocument);
        caseData.setCaseIssueFinalDecision(finalDecision);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        var data = details.getData();
        var cicCase = data.getCicCase();
        String caseNumber = data.getHyphenatedCaseRef();

        Document finalDecisionGuidance = getFinalDecisionGuidanceDocument(data, details.getId());
        data.getCaseIssueFinalDecision().setFinalDecisionGuidance(finalDecisionGuidance);

        final StringBuilder messageLine2 = new StringBuilder(100);
        messageLine2.append(" A notification will be sent  to: ");
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartySubject())) {
            messageLine2.append("Subject, ");
            caseFinalDecisionIssuedNotification.sendToSubject(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRepresentative())) {
            messageLine2.append("Representative, ");
            caseFinalDecisionIssuedNotification.sendToRepresentative(details.getData(), caseNumber);
        }
        if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyRespondent())) {
            messageLine2.append("Respondent, ");
            caseFinalDecisionIssuedNotification.sendToRespondent(details.getData(), caseNumber);
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Final decision notice issued %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();
    }

    private Document getFinalDecisionGuidanceDocument(CaseData caseData, Long caseId) {
        final String filename = FINAL_DECISION_ANNEX_FILE + LocalDateTime.now().format(formatter);

        return caseDataDocumentService.renderDocument(
            finalDecisionTemplateContent.apply(caseData, caseId),
            caseId,
            FINAL_DECISION_ANNEX_TEMPLATE_ID,
            LanguagePreference.ENGLISH,
            filename,
            request
        );

    }
}
