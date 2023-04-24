package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueCaseNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "issueCaseDocumentList= \"ALWAYS_HIDE\"";
    private static final String RECIPIENT_LABEL = "Issue Case information recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("issueCaseNotifyParties")
            .pageLabel("Notify other parties")
            .label("LabelIssueCaseNotifyParties", "")
            .complex(CaseData::getCicCase)
            .label("issueCaseNotifyPartiesMessage", "Which other parties should be notified that the case has been issued to respondent?")
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)
            .readonly(CicCase::getRespondentName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRespondent,
                "cicCaseRespondentName!=\"\" ", RECIPIENT_LABEL)
            .done();
    }

}

