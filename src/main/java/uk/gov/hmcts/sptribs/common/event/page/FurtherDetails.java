package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class FurtherDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("furtherDetailsObject")
            .pageLabel("Enter further details about this case")
            .label("LabelFurtherDetails", "")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getSchemeCic,"Scheme")
            .mandatory(CicCase::getClaimLinkedToCic)
            .mandatory(CicCase::getCicaReferenceNumber, "cicCaseClaimLinkedToCic = \"Yes\"")
            .mandatory(CicCase::getCompensationClaimLinkCIC)
            .mandatory(CicCase::getPoliceAuthority, "cicCaseCompensationClaimLinkCIC = \"Yes\"")
            .mandatory(CicCase::getFormReceivedInTime)
            .mandatory(CicCase::getMissedTheDeadLineCic, "cicCaseFormReceivedInTime = \"Yes\" OR cicCaseFormReceivedInTime = \"No\"")
            .done();
    }
}
