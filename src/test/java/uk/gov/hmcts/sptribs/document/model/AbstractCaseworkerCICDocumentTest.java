package uk.gov.hmcts.sptribs.document.model;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;

import static org.junit.Assert.assertTrue;

public class AbstractCaseworkerCICDocumentTest {

    @Test
    public void testGetValue() {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("test.pdf").build())
            .build();

        AbstractCaseworkerCICDocument abstractCaseworkerCICDocument = new AbstractCaseworkerCICDocument<>(document);

        assertTrue(abstractCaseworkerCICDocument.getValue().equals(document));
    }


}
