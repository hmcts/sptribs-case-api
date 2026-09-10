package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CloseCaseSelectRecipients implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "closeCloseCaseReason = \"ALWAYS_HIDE\"";

    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        new SelectRecipientsHelper().addTo(pageBuilder,
            "closeCaseSelectRecipients",
            "CloseCase",
            "Who should be notified of the decision to close this case?",
            "Close case recipient",
            ALWAYS_HIDE
        );
    }
}
