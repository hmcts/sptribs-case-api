package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateDecisionDocumentFormat;

public class IssueDecisionUploadNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionUploadNotice", this::midEvent)
            .pageLabel("Upload decision notice")
            .pageShowConditions(issueDecisionShowConditions())
            .label("labelIssueDecisionUpload",
                """

                    Upload a copy of the decision notice that you want to add to this case.

                    The decision notice should be:

                    - a maximum of 100MB in size (larger files must be split)

                    - labelled clearly, e.g. applicant-name-decision-notice.pdf



                    Note: If the remove button is disabled, please refresh the page to remove attachments""")
            .complex(CaseData::getCaseIssueDecision)
            .mandatoryWithLabel(CaseIssueDecision::getDecisionDocument, "Add a file")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        CICDocument uploadedDocument = data.getCaseIssueDecision().getDecisionDocument();
        final List<String> errors = validateDecisionDocumentFormat(uploadedDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
