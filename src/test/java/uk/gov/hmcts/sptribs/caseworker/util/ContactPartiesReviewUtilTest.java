package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.model.Slot;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPartiesReviewUtilTest {

    @Test
    void showsAllTenSelectedDocumentsAndClearsThemWhenSelectionIsEmpty() {
        List<ListValue<CaseworkerCICDocument>> documents = IntStream.rangeClosed(1, 10)
            .mapToObj(index -> {
                ListValue<CaseworkerCICDocument> value = new ListValue<>();
                value.setValue(CaseworkerCICDocument.builder()
                    .documentLink(Document.builder().filename("document" + index + ".pdf")
                        .url("https://example.test/documents/" + UUID.randomUUID()).build()).build());
                return value;
            }).toList();
        List<DynamicListElement> selection = documents.stream().map(value -> {
            Document document = value.getValue().getDocumentLink();
            return DynamicListElement.builder().code(UUID.randomUUID())
                .label("[" + document.getFilename() + "](" + document.getUrl() + "/binary)").build();
        }).toList();
        CaseData data = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(documents).build())
            .contactPartiesDocuments(ContactPartiesDocuments.builder()
                .documentList(DynamicMultiSelectList.builder().value(selection).build()).build())
            .build();

        ContactPartiesReviewUtil.setReviewDocuments(data);

        assertThat(data.getContactPartiesDocuments().getAct()).containsExactlyInAnyOrder(Slot.values());
        assertThat(data.getContactPartiesDocuments().getD01()).isEqualTo(documents.getFirst().getValue());
        assertThat(data.getContactPartiesDocuments().getD10()).isEqualTo(documents.get(9).getValue());

        data.getContactPartiesDocuments().getDocumentList().setValue(List.of());
        ContactPartiesReviewUtil.setReviewDocuments(data);

        assertThat(data.getContactPartiesDocuments().getAct()).isEmpty();
        assertThat(data.getContactPartiesDocuments().getD01()).isNull();
        assertThat(data.getContactPartiesDocuments().getD10()).isNull();

        ContactPartiesReviewUtil.clearReviewDocuments(data.getContactPartiesDocuments());
        assertThat(data.getContactPartiesDocuments().getAct()).isNull();
    }
}
