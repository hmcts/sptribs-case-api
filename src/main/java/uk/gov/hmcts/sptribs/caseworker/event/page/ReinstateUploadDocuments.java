package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ReinstateUploadDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("reinstateUploadDocument")
            .pageLabel("Upload documents")
            .label("LabelReinstateCaseUploadDocument", "")
            .complex(CaseData::getCicCase)
            .label("reinstateUploadMessage",
                "<b>Please upload any documents that explain why this case is being reinstated. (Optional)</b>")
            .label("reinstateUploadAdvice", """
                Files should be
                  *  uploaded separately and not in one large file
                  *  a maximum of 100MB in size (larger files must be split)
                  *  labelled clearly, e.g. applicant-name-B1-form.pdf

                Add a file
                Upload a file to the system
                """)
            .optionalWithLabel(CicCase::getReinstateDocuments, "Reinstate Documents")
            .done();

    }
}
