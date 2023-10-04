package uk.gov.hmcts.sptribs.e2e.enums;

import com.microsoft.playwright.Page;

import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;

public enum CaseParty {
    Subject {
        @Override
        public void select(Page page) {
            check(page, "Subject");
        }
    },
    Applicant {
        @Override
        public void select(Page page) {
            check(page, "Applicant (if different from subject)");
        }
    },
    Representative {
        @Override
        public void select(Page page) {
            check(page, "Representative");
        }
    };

    private static void check(Page page, String party) {
        getCheckBoxByLabel(page, party).check();
    }

    public abstract void select(Page page);
}
