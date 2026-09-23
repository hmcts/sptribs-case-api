package uk.gov.hmcts.sptribs.document.model;

import java.time.OffsetDateTime;

public record ContactPartyDocumentDetails(DocumentEntity document,
                                          OffsetDateTime sentOn) {
}
