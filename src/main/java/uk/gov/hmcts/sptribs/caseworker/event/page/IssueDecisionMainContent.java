package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueDecisionShowConditions;

public class IssueDecisionMainContent implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueDecisionMainContent")
            .pageLabel("Edit Decision")
            .label("LabelIssueDecisionMainContent", "")
            .pageShowConditions(issueDecisionShowConditions())
            .label("LabelIssueDecisionMainContentHeader",
                "<hr>" + "\n<h3>Header</h3>" + "\nThe header will be automatically generated."
                    + "You can preview this in pdf document on the next screen.\n\n"
                    + "<hr>\n"
                    + "<h3>Main content</h3>\n\n")
            .mandatory(CaseData::getDecisionMainContent)
            .label("LabelIssueDecisionMainContentFooter", "<hr><h3>Footer</h3>\n The footer will be automatically generated.\n "
                + "You can preview this in pdf document on the next screen.\n"
                + "<hr>\n")
            .done();
    }

}
