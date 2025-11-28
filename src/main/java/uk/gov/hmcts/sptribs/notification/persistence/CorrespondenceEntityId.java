package uk.gov.hmcts.sptribs.notification.persistence;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
public class CorrespondenceEntityId implements Serializable {
    private UUID id;

    private Long caseReferenceNumber;

    public CorrespondenceEntityId(UUID id, Long caseReferenceNumber) {
        this.id = id;
        this.caseReferenceNumber = caseReferenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CorrespondenceEntityId that = (CorrespondenceEntityId) o;
        return id.equals(that.id) && caseReferenceNumber.equals(that.caseReferenceNumber);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + caseReferenceNumber.hashCode();
        return result;
    }
}
