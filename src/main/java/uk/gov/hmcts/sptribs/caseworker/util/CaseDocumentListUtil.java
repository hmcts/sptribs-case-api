package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;

public final class CaseDocumentListUtil {
    private CaseDocumentListUtil() {
    }

    public static void updateCaseDocumentList(List<ListValue<CaseworkerCICDocument>> documents,
                                              DocumentType selectedDocumentCategory,
                                              String selectedDocumentEmailContent,
                                              Document selectedDocumentLink) {

        if (!CollectionUtils.isEmpty(documents)) {
            for (ListValue<CaseworkerCICDocument> document : documents) {
                if (selectedDocumentLink.getUrl().equals(document.getValue().getDocumentLink().getUrl())) {
                    document.getValue().getDocumentLink().setCategoryId(selectedDocumentCategory.getCategory());
                    document.getValue().setDocumentCategory(selectedDocumentCategory);
                    document.getValue().setDocumentEmailContent(selectedDocumentEmailContent);
                }
            }
        }
    }
}
