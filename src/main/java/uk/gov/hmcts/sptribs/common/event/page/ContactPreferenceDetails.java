package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ContactPreferenceDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("objectContacts")
            .label("objectContact", "Who should receive information about the case?")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getSubjectCIC,"cicCasePartiesCICCONTAINS \"SubjectCIC\"")
            .optional(CicCase::getApplicantCIC,"cicCasePartiesCICCONTAINS \"ApplicantCIC\"")
            .optional(CicCase::getRepresentativeCIC,"cicCasePartiesCICCONTAINS \"RepresentativeCIC\"")
            .done();
    }
}
