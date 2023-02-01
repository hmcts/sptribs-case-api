package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullRecordSubjectRepresentativeRespondent;

public class RecordNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "recordHearingType=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("recordListingNotifyPage", this::midEvent)
            .pageLabel("Notify parties")
            .label("LabelNotifyParties", "")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getRecordNotifyPartySubject,
                "cicCaseFullName!=\"\" ",
                "Listing information recipient - Subject")
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getRecordNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ",
                "Listing information recipient - Representative")
            .readonly(CicCase::getRespondantName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getRecordNotifyPartyRespondent,
                "cicCaseRespondantName!=\"\" ",
                "Listing information recipient - Respondent")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullRecordSubjectRepresentativeRespondent(data)) {
            errors.add("One recipient must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
