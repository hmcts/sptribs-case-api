package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPartiesReviewTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerialiseUsingUpperCamelCasePropertyNames() throws Exception {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url").binaryUrl("binary-url").filename("test.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> listValue = new ListValue<>();
        listValue.setValue(document);

        ContactPartiesReview review = ContactPartiesReview.builder()
            .previewDoc(List.of(listValue))
            .contactParties(ContactParties.builder().subjectContactParties(Set.of(SubjectCIC.SUBJECT)).build())
            .message("Message for parties")
            .build();

        String json = mapper.writeValueAsString(review);

        assertThat(json).contains("\"PreviewDoc\"");
        assertThat(json).contains("\"ContactParties\"");
        assertThat(json).contains("\"Message\"");
        assertThat(json).contains("\"subjectContactParties\"");
        assertThat(json).doesNotContain("\"previewDoc\"");
        assertThat(json).doesNotContain("\"contactParties\"");
    }

    @Test
    void shouldDeserialiseAndIgnoreUnknownProperties() throws Exception {
        String json = """
            {
              \"PreviewDoc\": [],
              \"ContactParties\": {
                \"subjectContactParties\": [\"SubjectCIC\"]
              },
              \"Message\": \"Review message\",
              \"UnknownField\": \"ignored\"
            }
            """;

        ContactPartiesReview review = mapper.readValue(json, ContactPartiesReview.class);

        assertThat(review.getPreviewDoc()).isEmpty();
        assertThat(review.getContactParties()).isNotNull();
        assertThat(review.getContactParties().getSubjectContactParties()).contains(SubjectCIC.SUBJECT);
        assertThat(review.getMessage()).isEqualTo("Review message");
    }
}
