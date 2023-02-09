package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;

public class IssueDecisionUploadNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionUploadNotice")
            .pageLabel("Upload decision notice")
            .pageShowConditions(issueDecisionShowConditions())
            .label("labelIssueDecisionUpload",
                "\nUpload a copy of the decision notice that you want to add to this case.\n"
                    + "\nThe decision notice should be:\n"
                    + "\n- a maximum of 100MB in size (larger files must be split)\n"
                    + "\n- labelled clearly, e.g. applicant-name-decision-notice.pdf\n")
            .complex(CaseData::getCaseIssueDecision)
            .mandatoryWithLabel(CaseIssueDecision::getDecisionDocument, "Add a file")
            .done();
    }
}
