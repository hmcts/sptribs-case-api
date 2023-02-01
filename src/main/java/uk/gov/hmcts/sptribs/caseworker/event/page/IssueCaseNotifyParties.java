package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueCaseNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "issueCaseAdditionalDocument = \"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("issueCaseNotifyParties")
            .pageLabel("Notify other parties")
            .label("LabelIssueCaseNotifyParties", "")
            .complex(CaseData::getCicCase)
            .label("issueCaseNotifyPartiesMessage", "Which other parties should be notified that the case has been issued to respondent?")
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ",
                "Issue Case information recipient - Subject")
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ",
                "Issue Case information recipient - Representative")
            .readonly(CicCase::getRespondantName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRespondent,
                "cicCaseRespondantName!=\"\" ",
                "Issue Case information recipient - Respondent")
            .done();
    }

}

