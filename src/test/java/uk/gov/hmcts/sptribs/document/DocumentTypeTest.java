package uk.gov.hmcts.sptribs.document;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTypeTest {

    DocumentType documentTypeBirthCert = DocumentType.BIRTH_OR_ADOPTION_CERTIFICATE;


    @Test
    public void shouldCheckIsNotNull() {
        assertThat(documentTypeBirthCert).isNotNull();
    }

    @Test
    public void shouldCheckInvalid() {
        assertThat(documentTypeBirthCert.getLabel()=="anything").isFalse();
    }
}
