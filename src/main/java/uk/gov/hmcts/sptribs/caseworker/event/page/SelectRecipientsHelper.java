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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = EventUtil.checkRecipient(data);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
