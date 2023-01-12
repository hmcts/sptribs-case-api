package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class IssueDecisionUploadNotice implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameSelectTemplate = "SelectTemplate";
        String pageNameUpload = "UploadNotice";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameSelectTemplate, "caseIssueDecisionDecisionNotice = \"Create from a template\"");
        map.put(pageNameUpload, "caseIssueDecisionDecisionNotice = \"Upload from your computer\"");

        pageBuilder.page(pageNameUpload)
            .pageLabel("Upload decision notice")
            .pageShowConditions(map)
            .label(pageNameUpload,
                "\nUpload a copy of the decision notice that you want to add to this case.\n"
                    + "\nThe decision notice should be:\n"
                    + "\n- a maximum of 100MB in size (larger files must be split)\n"
                    + "\n- labelled clearly, e.g. applicant-name-decision-notice.pdf\n")
            .complex(CaseData::getCaseIssueDecision)
            .mandatoryWithLabel(CaseIssueDecision::getDecisionDocument, "Add a file")
            .done();
    }
}
