package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class ContactPartiesSelectDocument implements CcdPageConfiguration {

    private static final int MAX_DOCUMENT_COUNT = 10;

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("contactPartiesSelectDocument", this::midEvent)
            .pageLabel("Documents to include")
            .label("LabelContactPartiesSelectDocument", "Select a case document to include with this message(Optional)")
            .label("LabelContactPartiesSelectDocumentNote",
                "Note: Gov.Notify only supports sending documents in the formats of PDF, CSV, txt, rtf, MS Word Document "
                    + "file and MS Excel File. Your file must be smaller than 2MB")
            .complex(CaseData::getContactPartiesDocuments)
            .optionalWithLabel(ContactPartiesDocuments::getDocumentList,"Selected Documents")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        DynamicMultiSelectList list = data.getContactPartiesDocuments().getDocumentList();
        if (null != list && list.getValue().size() > MAX_DOCUMENT_COUNT) {
            errors.add("Select up to 10 documents");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
