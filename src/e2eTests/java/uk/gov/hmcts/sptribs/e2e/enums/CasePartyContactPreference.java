package uk.gov.hmcts.sptribs.e2e.enums;

import com.microsoft.playwright.Page;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.e2e.enums.ContactPreference.Email;
import static uk.gov.hmcts.sptribs.e2e.enums.ContactPreference.Post;
import static uk.gov.hmcts.sptribs.testutils.PageHelpers.getCheckBoxByLabel;

public enum CasePartyContactPreference {
    Subject {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Subject, Email);
            return h;
        }
    },
    SubjectPost {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Subject, Post);
            return h;
        }
    },
    SubjectEmail {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Subject, Email);
            return h;
        }
    },
    Applicant {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Applicant (if different from subject)");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Applicant, Email);
            return h;
        }
    },
    ApplicantPost {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Applicant (if different from subject)");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Applicant, Post);
            return h;
        }
    },
    ApplicantEmail {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Applicant (if different from subject)");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Applicant, Email);
            return h;
        }
    },
    Representative {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Representative");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Representative, Email);
            return h;
        }
    },
    RepresentativePost {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Representative");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Representative, Post);
            return h;
        }
    },
    RepresentativeEmail {
        @Override
        public Map<CaseParty, ContactPreference> select(Page page) {
            check(page, "Representative");
            Map<CaseParty, ContactPreference> h = new HashMap<>();
            h.put(CaseParty.Representative, Email);
            return h;
        }
    };

    private static void check(Page page, String party) {
        getCheckBoxByLabel(page, party).check();
    }

    public abstract Map<CaseParty, ContactPreference> select(Page page);
}
