package uk.gov.hmcts.sptribs.notification.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "case_correspondences")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@IdClass(CorrespondenceEntityId.class)
public class CorrespondenceEntity {

    @Id
    private UUID id;

    @Id
    private Long caseReferenceNumber;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "sent_on", nullable = false)
    private OffsetDateTime sentOn;

    @Column(name = "sent_from", nullable = false)
    private String sentFrom;

    @Column(name = "sent_to", nullable = false)
    private String sentTo;

    @Column(name = "correspondence_type", nullable = false)
    private String correspondenceType;

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
        CorrespondenceEntity that = (CorrespondenceEntity) correspondenceEntityObject;
        return getId() != null && Objects.equals(getId(), that.getId())
            && getCaseReferenceNumber() != null && Objects.equals(getCaseReferenceNumber(), that.getCaseReferenceNumber());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id, caseReferenceNumber);
    }
}
