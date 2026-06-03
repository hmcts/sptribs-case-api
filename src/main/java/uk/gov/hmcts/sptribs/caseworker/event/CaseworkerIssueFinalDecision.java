package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionUpload;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.services.DocumentsService;
import uk.gov.hmcts.sptribs.notification.dispatcher.CaseFinalDecisionIssuedNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_TO_DATABASE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_SAVING_DOCUMENT_WITH_NO_FILENAME_TO_DATABASE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.generateSimpleErrorMessageDocumentSave;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerIssueFinalDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final CcdPageConfiguration issueFinalDecisionNotice = new IssueFinalDecisionNotice();

    private static final IssueFinalDecisionSelectTemplate issueFinalDecisionSelectTemplate = new IssueFinalDecisionSelectTemplate();

    private static final CcdPageConfiguration issueFinalDecisionPreviewTemplate = new IssueFinalDecisionPreviewTemplate();

    private static final CcdPageConfiguration issueFinalDecisionSelectRecipients = new IssueFinalDecisionSelectRecipients();

    private static final CcdPageConfiguration issueFinalDecisionMainContent = new IssueFinalDecisionMainContent();

    private static final CcdPageConfiguration issueFinalDecisionUpload = new IssueFinalDecisionUpload();

    private final CcdPageConfiguration issueFinalDecisionFooter;

    private final HttpServletRequest request;

    private final CaseDataDocumentService caseDataDocumentService;

    private final CaseFinalDecisionIssuedNotification caseFinalDecisionIssuedNotification;

    private final Clock clock;

    private final DocumentsService documentsService;

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
                    ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_SENIOR_JUDGE, ST_CIC_JUDGE, ST_CIC_WA_CONFIG_USER)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
        issueFinalDecisionMainContent.addTo(pageBuilder);
        issueFinalDecisionUpload.addTo(pageBuilder);
        issueFinalDecisionFooter.addTo(pageBuilder);
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

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final CICDocument finalDecisionDocument = caseData.getCaseIssueFinalDecision().getDocument();

        List<String> errors = new ArrayList<>();

        if (finalDecisionDocument != null) {
            finalDecisionDocument.getDocumentLink().setCategoryId("TD");
            try {
                documentsService.buildAndSaveNewDocumentEntity(
                    finalDecisionDocument.getDocumentLink(),
                    details.getId(),
                    false
                );
            } catch (RuntimeException e) {
                errors.add(generateSimpleErrorMessageDocumentSave(finalDecisionDocument.getDocumentLink(), e.getMessage()));
            }
        }

        caseData.getCaseIssueFinalDecision().setFinalDecisionDate(LocalDate.now(this.clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(CaseClosed)
            .errors(errors)
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
