package uk.gov.hmcts.sptribs.notification;

import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_LINK;

public enum EmailBundleCreatedResponses {

    RESPONDENT_RESPONSE(
        "We’re sharing the hearing bundle with you, in preparation for the upcoming hearing."
            + " Please access the documents by logging into manage cases and viewing the bundles tab."
    ),
    REPRESENTATIVE_APPLICANT_RESPONSE(
        "We’re sharing the hearing bundle with you, in preparation for your upcoming hearing."
            + " Please download this by logging into the dashboard "
            + DASHBOARD_LINK
    );

    private final String template;

    EmailBundleCreatedResponses(String template) {
        this.template = template;
    }

    public String getEmailResponse() {
        return template;
    }
}
