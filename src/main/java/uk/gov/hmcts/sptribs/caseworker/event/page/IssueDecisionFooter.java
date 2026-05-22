package uk.gov.hmcts.sptribs.caseworker.event.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.DecisionTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DECISION_FILE;

@Component
@RequiredArgsConstructor
public class IssueDecisionFooter implements CcdPageConfiguration {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CaseDataDocumentService caseDataDocumentService;
    private final DecisionTemplateContent decisionTemplateContent;
    private final HttpServletRequest request;

    @Override
    public void addTo(PageBuilder pageBuilder) {
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
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
}
