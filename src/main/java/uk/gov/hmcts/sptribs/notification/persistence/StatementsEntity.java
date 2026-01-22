package uk.gov.hmcts.sptribs.notification.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "case_statements")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class StatementsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long caseReferenceNumber;

    @Column(name = "created_on", nullable = false)
    private OffsetDateTime createdOn;

    @Column(name = "party_type", nullable = false)
    private String partyType;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_filename", nullable = false)
    private String documentFilename;

    @Column(name = "document_binary_url", nullable = false)
    private String documentBinaryUrl;

    @Override
    public final boolean equals(Object correspondenceEntityObject) {
        if (this == correspondenceEntityObject) {
            return true;
        }
        if (correspondenceEntityObject == null) {
            return false;
        }
        Class<?> correspondenceEntityObjectEffectiveClass =
            correspondenceEntityObject instanceof HibernateProxy ? ((HibernateProxy) correspondenceEntityObject)
            .getHibernateLazyInitializer().getPersistentClass() : correspondenceEntityObject.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this)
            .getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != correspondenceEntityObjectEffectiveClass) {
            return false;
        }
        StatementsEntity that = (StatementsEntity) correspondenceEntityObject;
        return getId() != null && Objects.equals(getId(), that.getId())
            && getCaseReferenceNumber() != null && Objects.equals(getCaseReferenceNumber(), that.getCaseReferenceNumber());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
