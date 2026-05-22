package uk.gov.hmcts.sptribs.caseworker.event.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_FILE;

@Component
@RequiredArgsConstructor
public class IssueFinalDecisionFooter implements CcdPageConfiguration {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CaseDataDocumentService caseDataDocumentService;
    private final FinalDecisionTemplateContent finalDecisionTemplateContent;
    private final HttpServletRequest request;

    @Override
    public void addTo(PageBuilder pageBuilder) {
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
}
