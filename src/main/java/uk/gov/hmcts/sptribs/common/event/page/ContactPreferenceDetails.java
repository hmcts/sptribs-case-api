package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;


public class ContactPreferenceDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("objectContacts",this::midEvent)
            .label("objectContact", "Who should receive information about the case?")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getSubjectCIC,"cicCasePartiesCICCONTAINS \"SubjectCIC\"")
            .optional(CicCase::getApplicantCIC,"cicCasePartiesCICCONTAINS \"ApplicantCIC\"")
            .optional(CicCase::getRepresentativeCIC,"cicCasePartiesCICCONTAINS \"RepresentativeCIC\"")
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase() && (null == data.getCicCase().getSubjectCIC() || data.getCicCase().getSubjectCIC().size() == 0)
            && (null == data.getCicCase().getRepresentativeCIC() || data.getCicCase().getRepresentativeCIC().size() == 0)
            && (null == data.getCicCase().getApplicantCIC() || data.getCicCase().getApplicantCIC().size() == 0)) {
            errors.add("One field must be selected.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
