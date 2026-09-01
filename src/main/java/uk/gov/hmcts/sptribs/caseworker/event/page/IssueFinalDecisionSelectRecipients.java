package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueFinalDecisionSelectRecipients implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "caseIssueFinalDecisionFinalDecisionNotice = \"ALWAYS_HIDE\"";

    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        new SelectRecipientsHelper().addTo(pageBuilder,
            "issueFinalDecisionSelectRecipients",
            "IssueFinalDecision",
            "Who should receive this decision notice?",
            "Final Decision information recipient",
            ALWAYS_HIDE
        );
    }
}
