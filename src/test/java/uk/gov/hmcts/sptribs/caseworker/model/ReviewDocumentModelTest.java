package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewDocumentModelTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void caseIssuePreservesReviewFieldsInJsonAndBuilder() throws Exception {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("issued.pdf").build()).build();
        DynamicMultiSelectList selection = DynamicMultiSelectList.builder().value(List.of()).build();
        CaseIssue issue = CaseIssue.builder()
            .activeDocumentSlots(Set.of(Slot.D01))
            .d01(document)
            .reviewSelectedParties("Subject")
            .documentList(selection)
            .build();

        JsonNode json = objectMapper.valueToTree(issue);

        assertThat(json.get("ActiveDocumentSlots").get(0).asText()).isEqualTo("D01");
        assertThat(json.get("D01").toString()).contains("issued.pdf");
        assertThat(json.get("ReviewSelectedParties").asText()).isEqualTo("Subject");
        assertThat(json.get("DocumentList")).isNotNull();

        CaseIssue restored = objectMapper.treeToValue(json, CaseIssue.class);
        assertThat(restored.getActiveDocumentSlots()).containsExactly(Slot.D01);
        assertThat(restored.getD01().getDocumentLink().getFilename()).isEqualTo("issued.pdf");
        assertThat(restored.getReviewSelectedParties()).isEqualTo("Subject");
        assertThat(restored.getDocumentList()).isNotNull();
        assertThat(issue.toBuilder().build()).isEqualTo(issue);
    }

    @Test
    void contactPartiesPreservesReviewFieldsInJsonAndBuilder() throws Exception {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("contact.pdf").build()).build();
        DynamicMultiSelectList selection = DynamicMultiSelectList.builder().value(List.of()).build();
        ContactPartiesDocuments contact = ContactPartiesDocuments.builder()
            .activeDocumentSlots(Set.of(Slot.D01))
            .d01(document)
            .reviewSelectedParties("Applicant")
            .reviewMessage("Message")
            .documentList(selection)
            .build();

        JsonNode json = objectMapper.valueToTree(contact);

        assertThat(json.get("ActiveDocumentSlots").get(0).asText()).isEqualTo("D01");
        assertThat(json.get("D01").toString()).contains("contact.pdf");
        assertThat(json.get("ReviewSelectedParties").asText()).isEqualTo("Applicant");
        assertThat(json.get("ReviewMessage").asText()).isEqualTo("Message");
        assertThat(json.get("DocumentList")).isNotNull();

        ContactPartiesDocuments restored = objectMapper.treeToValue(json, ContactPartiesDocuments.class);
        assertThat(restored.getActiveDocumentSlots()).containsExactly(Slot.D01);
        assertThat(restored.getD01().getDocumentLink().getFilename()).isEqualTo("contact.pdf");
        assertThat(restored.getReviewSelectedParties()).isEqualTo("Applicant");
        assertThat(restored.getReviewMessage()).isEqualTo("Message");
        assertThat(restored.getDocumentList()).isNotNull();
        assertThat(contact.toBuilder().build()).isEqualTo(contact);
    }
}
