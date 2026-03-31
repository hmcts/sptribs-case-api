package uk.gov.hmcts.sptribs.document.persistence;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
public class DocumentEntityId implements Serializable {
    private UUID id;

    private Long caseReferenceNumber;

    public DocumentEntityId(UUID id, Long caseReferenceNumber) {
        this.id = id;
        this.caseReferenceNumber = caseReferenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        uk.gov.hmcts.sptribs.document.persistence.DocumentEntityId that
            = (uk.gov.hmcts.sptribs.document.persistence.DocumentEntityId) o;
        return id.equals(that.id) && caseReferenceNumber.equals(that.caseReferenceNumber);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + caseReferenceNumber.hashCode();
        return result;
    }
}

