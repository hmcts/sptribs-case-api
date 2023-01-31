package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionNotice;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionPreviewTemplate;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectRecipients;
import uk.gov.hmcts.sptribs.caseworker.event.page.IssueFinalDecisionSelectTemplate;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerIssueFinalDecision implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration issueFinalDecisionNotice = new IssueFinalDecisionNotice();

    @Autowired
    private IssueFinalDecisionSelectTemplate issueFinalDecisionSelectTemplate;

    private static final CcdPageConfiguration issueFinalDecisionPreviewTemplate = new IssueFinalDecisionPreviewTemplate();

    private static final CcdPageConfiguration issueFinalDecisionSelectRecipients = new IssueFinalDecisionSelectRecipients();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_FINAL_DECISION)
            .forStates(AwaitingOutcome)
            .name("Decision: Issue final decision")
            .description("Decision: Issue final decision")
            .showEventNotes()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
        issueFinalDecisionNotice.addTo(pageBuilder);
        issueFinalDecisionSelectTemplate.addTo(pageBuilder);
        uploadDocuments(pageBuilder);
        issueFinalDecisionAddDocumentFooter(pageBuilder);
        issueFinalDecisionPreviewTemplate.addTo(pageBuilder);
        issueFinalDecisionSelectRecipients.addTo(pageBuilder);
    }

    private void uploadDocuments(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "issueFinalDecisionSelectTemplate";
        String pageNamePreviewTemplate = "issueFinalDecisionPreviewTemplate";
        String pageNameUpload = "issueFinalDecisionUpload";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueFinalDecisionFinalDecisionNotice = \"Create from a template\"");
        map.put(pageNamePreviewTemplate, "caseIssueFinalDecisionFinalDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueFinalDecisionFinalDecisionNotice = \"Upload from your computer\"");
        pageBuilder.page(pageNameUpload)
            .pageLabel("Upload decision notice")
            .pageShowConditions(map)
            .label("LabelDoc",
                "\nUpload a copy of the decision notice that you want to add to this case.\n"
                    + "\n<h3>The decision notice should be:</h3>\n"
                    + "\n- a maximum of 100MB in size (larger files must be split)\n"
                    + "\n- labelled clearly, e.g. applicant-name-decision-notice.pdf\n\n")
            .complex(CaseData::getCaseIssueFinalDecision)
            .optionalWithLabel(CaseIssueFinalDecision::getDocuments, "File Attachments")
            .done();
    }

    private void issueFinalDecisionAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("addDocumentFooter")
            .pageLabel("Document footer")
            .label("LabelDocFooter",
                "\nDecision Notice Signature\n"
                    + "\nConfirm the Role and Surname of the person who made this decision - this will be added"
                    + " to the bottom of the generated decision notice. E.g. 'Tribunal Judge Farrelly'")
            .mandatory(CaseData::getFinalDecisionSignature)
            .done();
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
        var cicCase = details.getData().getCicCase();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Final decision notice issued %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();
    }
}
