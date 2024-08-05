package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerSelectedCICDocument;

import java.util.List;

public final class CaseDocumentListUtil {
    private CaseDocumentListUtil() {
    }

    public static void updateCaseDocumentList(List<ListValue<CaseworkerCICDocument>> documents,
                                              CaseworkerSelectedCICDocument selectedDocument) {

        if (!CollectionUtils.isEmpty(documents)) {
            for (ListValue<CaseworkerCICDocument> document : documents) {
                if (selectedDocument.getDocumentLink().getUrl().equals(document.getValue().getDocumentLink().getUrl())) {
                    document.getValue().setDocumentCategory(selectedDocument.getDocumentCategory());
                    document.getValue().getDocumentLink().setCategoryId(selectedDocument.getDocumentCategory().getCategory());
                    document.getValue().setDocumentEmailContent(selectedDocument.getDocumentEmailContent());
                }
            }
        }
    }
}
