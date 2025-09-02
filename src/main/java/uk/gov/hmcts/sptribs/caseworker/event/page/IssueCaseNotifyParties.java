package uk.gov.hmcts.sptribs.caseworker.event.page;

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

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeRespondent;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;

public class IssueCaseNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "issueCaseDocumentList= \"ALWAYS_HIDE\"";
    private static final String RECIPIENT_LABEL = "Issue Case information recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("issueCaseNotifyParties", this::midEvent)
            .pageLabel("Notify other parties")
            .label("LabelIssueCaseNotifyParties", "")
            .complex(CaseData::getCicCase)
            .label("issueCaseNotifyPartiesMessage", "Which other parties should be notified that the case has been issued to respondent?")
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseFullName}")
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseRepresentativeFullName}")
            .readonly(CicCase::getRespondentName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondentName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseRespondentName}")
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyApplicant, "cicCaseApplicantFullName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseApplicantFullName}")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeRespondent(caseData)) {
            errors.add(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
        } else if ((caseData.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || caseData.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !CollectionUtils.isEmpty(caseData.getCicCase().getNotifyPartySubject())) {
            errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

}

