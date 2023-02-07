package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeRespondent;

public final class SelectRecipientsHelper {

    public void addTo(PageBuilder pageBuilder,
                             String pageId,
                             String labelPrefix,
                             String label,
                             String fieldLabelPrefix,
                             String alwaysHide) {
        pageBuilder
            .page(pageId, this::midEvent)
            .pageLabel("Select recipients")
            .label("Label" + labelPrefix + "SelectRecipientsEmpty", "")
            .label("label" + labelPrefix + "SelectRecipients", label)
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getFullName, alwaysHide)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ",
                fieldLabelPrefix + " recipient - Subject")
            .readonly(CicCase::getRepresentativeFullName, alwaysHide)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ",
                fieldLabelPrefix + " recipient - Representative")
            .readonly(CicCase::getRespondentName, alwaysHide)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRespondent,
                "cicCaseRespondentName!=\"\" ",
                fieldLabelPrefix + " recipient - Respondent")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeRespondent(data)) {
            errors.add("One recipient must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
