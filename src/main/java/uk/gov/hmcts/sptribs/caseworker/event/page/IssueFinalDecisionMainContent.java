package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.issueFinalDecisionShowConditions;


public class IssueFinalDecisionMainContent implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("issueFinalDecisionMainContent")
            .pageLabel("Edit Final Decision")
            .label("LabelIssueFinalDecisionMainContent", "")
            .pageShowConditions(issueFinalDecisionShowConditions())
            .label("LabelIssueFinalDecisionMainContentHeader",
                """
                    <hr>
                    <h3>Header</h3>
                    The header will be automatically generated.You can preview this in pdf document on the next screen.

                    <hr>
                    <h3>Main content</h3>\s
                    Enter text in the box below. This will be added into the centre of the generated decision document""")
            .mandatory(CaseData::getDecisionMainContent)
            .label("LabelIssueFinalDecisionMainContentFooter", """
                <hr><h3>Footer</h3>
                 The footer will be automatically generated.
                 You can preview this in pdf document on the next screen.
                <hr>
                """)
            .done();
    }

}
