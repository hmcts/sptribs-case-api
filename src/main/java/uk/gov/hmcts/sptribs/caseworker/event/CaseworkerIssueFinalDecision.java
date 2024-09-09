package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.notification.CaseFinalDecisionIssuedNotification;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_ANNEX_FILE;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_ANNEX_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_FILE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateDecisionDocumentFormat;

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

    @Value("${feature.wa.enabled}")
    private boolean isWorkAllocationEnabled;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ISSUE_FINAL_DECISION)
                .forStates(AwaitingOutcome)
                .name("Decision: Issue final decision")
                .description("Decision: Issue final decision")
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
        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
        issueFinalDecisionMainContent.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        issueFinalDecisionAddDocumentFooter(pageBuilder);
        issueFinalDecisionPreviewTemplate.addTo(pageBuilder);
        issueFinalDecisionSelectRecipients.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        final CaseData caseData = details.getData();

        caseData.setDecisionSignature("");

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void uploadDocuments(PageBuilder pageBuilder) {

        pageBuilder.page("issueFinalDecisionUpload", this::uploadDocumentMidEvent)
            .pageLabel("Upload decision notice")
            .pageShowConditions(issueFinalDecisionShowConditions())
            .label("LabelDoc", """
                Upload a copy of the decision notice that you want to add to this case.
                  *  <h3>The decision notice should be:</h3>
                  *  a maximum of 100MB in size (larger files must be split)
                  *  labelled clearly, e.g. applicant-name-decision-notice.pdf




                  Note: If the remove button is disabled, please refresh the page to remove attachments
                """
            )
            .complex(CaseData::getCaseIssueFinalDecision)
            .mandatoryWithLabel(CaseIssueFinalDecision::getDocument, "File Attachments")
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final CaseIssueFinalDecision finalDecision = caseData.getCaseIssueFinalDecision();
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

    public AboutToStartOrSubmitResponse<CaseData, State> uploadDocumentMidEvent(CaseDetails<CaseData, State> details,
                                                                                 CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        CICDocument uploadedDocument = data.getCaseIssueFinalDecision().getDocument();
        final List<String> errors = validateDecisionDocumentFormat(uploadedDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final CICDocument finalDecisionDocument = caseData.getCaseIssueFinalDecision().getDocument();

        if (finalDecisionDocument != null) {
            finalDecisionDocument.getDocumentLink().setCategoryId("TD");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData data = details.getData();
        final CicCase cicCase = data.getCicCase();
        final String caseNumber = data.getHyphenatedCaseRef();

        Document finalDecisionGuidance = getFinalDecisionGuidanceDocument(details.getId());
        data.getCaseIssueFinalDecision().setFinalDecisionGuidance(finalDecisionGuidance);
        try {
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
            if (!CollectionUtils.isEmpty(cicCase.getNotifyPartyApplicant())) {
                messageLine2.append("Applicant ");
                caseFinalDecisionIssuedNotification.sendToApplicant(details.getData(), caseNumber);
            }
        } catch (Exception notificationException) {
            log.error("Issue final decision notification failed with exception : {}", notificationException.getMessage());
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Issue final decision notification failed %n## Please resend the notification"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Final decision notice issued %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();
    }

    private Document getFinalDecisionGuidanceDocument(Long caseId) {
        final String filename = FINAL_DECISION_ANNEX_FILE + LocalDateTime.now().format(formatter);
        Map<String, Object> templateContent = new HashMap<>();

        return caseDataDocumentService.renderDocument(
            templateContent,
            caseId,
            FINAL_DECISION_ANNEX_TEMPLATE_ID,
            LanguagePreference.ENGLISH,
            filename,
            request
        );

    }
}
