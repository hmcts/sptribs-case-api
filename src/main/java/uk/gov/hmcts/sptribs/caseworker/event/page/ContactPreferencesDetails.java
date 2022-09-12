package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferencesDetailsCIC;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ContactPreferencesDetails implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("editContactPreferencesDetails")
            .label("objectContact", "Who should receive information about the case?")
            .complex(CaseData::getCicCase)
            .complex(CicCase::getContactPreferenceCic, "")
            .optional(ContactPreferencesDetailsCIC::getSubjectCIC, "cicCasePartiesCICCONTAINS \"SubjectCIC\"")
            .optional(ContactPreferencesDetailsCIC::getApplicantCIC, "cicCasePartiesCICCONTAINS \"ApplicantCIC\"")
            .optional(ContactPreferencesDetailsCIC::getRepresentativeCic, "cicCasePartiesCICCONTAINS \"RepresentativeCIC\"")
            // .mandatoryWithLabel(CicCase::getPartiesCIC, "")
            .done();


        //pageBuilder.page("objectSubjects", this::midEvent)
        // .label("subjectObject", "Which parties are named on the tribunal form?\r\n" + "\r\nCase record for [DRAFT]")
        //.pageLabel("Who are the parties in this case?")
        //.complex(CaseData::getCicCase)
        //.mandatoryWithLabel(CicCase::getPartiesCIC, "")
        //            .done();
    }

    /*
    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase() && !data.getCicCase().getPartiesCIC().contains(PartiesCIC.SUBJECT)) {
            errors.add("Subject is mandatory.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
    */

}


