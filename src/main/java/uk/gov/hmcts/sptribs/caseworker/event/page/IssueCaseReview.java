package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Component
public class IssueCaseReview implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("issueCaseReview")
            .pageLabel("Check your answers")
            .complex(CaseData::getCaseIssue)
            .readonly(CaseIssue::getD01, "issueCaseActiveDocumentSlotsCONTAINS \"D01\"")
            .readonly(CaseIssue::getD02, "issueCaseActiveDocumentSlotsCONTAINS \"D02\"")
            .readonly(CaseIssue::getD03, "issueCaseActiveDocumentSlotsCONTAINS \"D03\"")
            .readonly(CaseIssue::getD04, "issueCaseActiveDocumentSlotsCONTAINS \"D04\"")
            .readonly(CaseIssue::getD05, "issueCaseActiveDocumentSlotsCONTAINS \"D05\"")
            .readonly(CaseIssue::getD06, "issueCaseActiveDocumentSlotsCONTAINS \"D06\"")
            .readonly(CaseIssue::getD07, "issueCaseActiveDocumentSlotsCONTAINS \"D07\"")
            .readonly(CaseIssue::getD08, "issueCaseActiveDocumentSlotsCONTAINS \"D08\"")
            .readonly(CaseIssue::getD09, "issueCaseActiveDocumentSlotsCONTAINS \"D09\"")
            .readonly(CaseIssue::getD10, "issueCaseActiveDocumentSlotsCONTAINS \"D10\"")
            .readonly(CaseIssue::getReviewSelectedParties)
            .done();
    }
}
