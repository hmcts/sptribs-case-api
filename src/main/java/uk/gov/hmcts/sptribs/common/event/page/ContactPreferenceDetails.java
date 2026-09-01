package uk.gov.hmcts.sptribs.common.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;


public class ContactPreferenceDetails implements CcdPageConfiguration {
    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        pageBuilder
            .page("objectContacts", this::midEvent)
            .pageLabel("Who should receive information about the case?")
            .label("LabelObjectContacts", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getSubjectCIC, "cicCasePartiesCICCONTAINS \"SubjectCIC\"",
                "", "", "${cicCaseFullName}")
            .optional(CicCase::getApplicantCIC, "cicCasePartiesCICCONTAINS \"ApplicantCIC\"",
                "", "", "${cicCaseApplicantFullName}")
            .optional(CicCase::getRepresentativeCIC, "cicCasePartiesCICCONTAINS \"RepresentativeCIC\"",
                "", "", "${cicCaseRepresentativeFullName}")
            .done();
    }


    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> detailsBefore) {
        final T data = details.getData();
        final List<String> errors = new ArrayList<>();


        if (null != data.getCicCase() && CollectionUtils.isEmpty(data.getCicCase().getSubjectCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getRepresentativeCIC())
            && CollectionUtils.isEmpty(data.getCicCase().getApplicantCIC())) {
            errors.add(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
        } else if ((data.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || data.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !CollectionUtils.isEmpty(data.getCicCase().getSubjectCIC())) {
            errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
