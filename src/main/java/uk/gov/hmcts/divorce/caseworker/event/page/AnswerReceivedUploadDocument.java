package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.CaseDocuments;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class AnswerReceivedUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getAnswerReceivedSupportingDocuments)
                .done()
            .done();
    }
}
