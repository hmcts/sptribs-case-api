package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateDecisionDocumentFormat;

public class IssueFinalDecisionUpload implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueFinalDecisionUpload", this::midEvent)
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        CICDocument uploadedDocument = data.getCaseIssueFinalDecision().getDocument();
        final List<String> errors = validateDecisionDocumentFormat(uploadedDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
