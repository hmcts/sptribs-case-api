package uk.gov.hmcts.sptribs.document;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentIdProvider {

    public String documentId() {
        return UUID.randomUUID().toString();
    }
}
