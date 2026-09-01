package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.List;

public final class SelectRecipientsHelper {


    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder,
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
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ",
                "", fieldLabelPrefix, "${cicCaseFullName}")
            .readonly(CicCase::getRepresentativeFullName, alwaysHide)
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ",
                "", fieldLabelPrefix, "${cicCaseRepresentativeFullName}")
            .readonly(CicCase::getRespondentName, alwaysHide)
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondentName!=\"\" ",
                "", fieldLabelPrefix, "${cicCaseRespondentName}")
            .readonly(CicCase::getApplicantFullName, alwaysHide)
            .optional(CicCase::getNotifyPartyApplicant, "cicCaseApplicantFullName!=\"\"",
                "", fieldLabelPrefix, "${cicCaseApplicantFullName}")
            .done();
    }

    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> detailsBefore) {
        final T data = details.getData();
        final List<String> errors = EventUtil.checkRecipient(data);
        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
