package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class DocumentManagementUtil {
    public static final CICDocument EMPTY_DOCUMENT = new CICDocument();

    private DocumentManagementUtil() {

    }

    public static void addToRemovedDocuments(CicCase cicCase, CaseworkerCICDocument caseworkerCICDocument) {
        if (CollectionUtils.isEmpty(cicCase.getRemovedDocumentList())) {
            List<ListValue<CaseworkerCICDocument>> listValues = new ArrayList<>();

            ListValue<CaseworkerCICDocument> listValue = ListValue
                .<CaseworkerCICDocument>builder()
                .id("1")
                .value(caseworkerCICDocument)
                .build();

            listValues.add(listValue);

            cicCase.setRemovedDocumentList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<CaseworkerCICDocument> listValue = ListValue
                .<CaseworkerCICDocument>builder()
                .value(caseworkerCICDocument)
                .build();

            cicCase.getRemovedDocumentList().add(0, listValue); // always add new note as first element so that it is displayed on top

            cicCase.getRemovedDocumentList().forEach(
                removedFileListValue -> removedFileListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
    }

    public static void checkLists(CaseData caseData, List<ListValue<CaseworkerCICDocument>> oldList,
                                  List<ListValue<CaseworkerCICDocument>> updatedList) {
        if (CollectionUtils.isEmpty(updatedList)) {
            for (ListValue<CaseworkerCICDocument> doc : oldList) {
                addToRemovedDocuments(caseData.getCicCase(), doc.getValue());
            }
        } else {
            for (ListValue<CaseworkerCICDocument> cicDocumentListValue : oldList) {
                boolean found = false;
                for (ListValue<CaseworkerCICDocument> doc : updatedList) {
                    if (cicDocumentListValue.getValue().equals(doc.getValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    addToRemovedDocuments(caseData.getCicCase(), cicDocumentListValue.getValue());
                }
            }
        }

    }

    public static List<ListValue<CaseworkerCICDocument>> buildListValues(List<CaseworkerCICDocument> docList) {
        return docList.stream()
            .map(doc -> ListValue
                .<CaseworkerCICDocument>builder()
                .id(UUID.randomUUID().toString())
                .value(doc)
                .build())
            .toList();
    }




}
