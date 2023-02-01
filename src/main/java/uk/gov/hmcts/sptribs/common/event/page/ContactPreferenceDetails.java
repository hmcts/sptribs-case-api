package uk.gov.hmcts.sptribs.common.event.page;

import org.springframework.util.CollectionUtils;
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
        pageBuilder
            .page("objectContacts", this::midEvent)
            .pageLabel("Who should receive information about the case?")
            .label("LabelObjectContacts", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getSubjectCIC, "cicCasePartiesCICCONTAINS \"SubjectCIC\"")
            .optional(CicCase::getApplicantCIC, "cicCasePartiesCICCONTAINS \"ApplicantCIC\"")
            .optional(CicCase::getRepresentativeCIC, "cicCasePartiesCICCONTAINS \"RepresentativeCIC\"")
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();


        if (null != data.getCicCase() && CollectionUtils.isEmpty(data.getCicCase().getSubjectCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getRepresentativeCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getApplicantCIC())) {
            errors.add("One field must be selected.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
