package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SelectRecipientsHelper {

    public static void addTo(PageBuilder pageBuilder,
                             String pageId,
                             final MidEvent<CaseData, State> callback,
                             String labelPrefix,
                             String label,
                             String fieldLabelPrefix,
                             String alwaysHide) {
        pageBuilder
            .page(pageId, callback)
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
}
