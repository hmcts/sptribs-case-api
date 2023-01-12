package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderMainContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class DraftOrderMainContentPage implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("mainContent")
            .pageLabel("Edit order")
            .label("EditDraftOrderMainContent", "<hr>" + "\n<h3>Header</h3>" + "\nThe header will be automatically generated."
                + "You can preview this in pdf document on the next screen.\n\n"
                + "<hr>\n"
                + "<h3>Main content</h3>\n\n"
                + "Enter text in the box below.This will be added into the centre"
                + " of the generated order document.\n")
            .complex(CaseData::getDraftOrderMainContentCIC)
            .optional(DraftOrderMainContentCIC::getMainContent)
            .done()
            .label("footer", "<h2>Footer</h2>\n The footer will be automatically generated.\n "
                + "You can preview this in pdf document on the next screen.\n"
                + "<hr>\n")
            .done();
    }


}
