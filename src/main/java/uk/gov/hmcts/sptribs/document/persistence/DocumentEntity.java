package uk.gov.hmcts.sptribs.document.persistence;

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
@Table(name = "case_documents")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@IdClass(DocumentEntityId.class)
public class DocumentEntity {

    @Id
    private UUID id;

    @Id
    private Long caseReferenceNumber;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_filename", nullable = false)
    private String documentFilename;

    @Column(name = "document_binary_url", nullable = false)
    private String documentBinaryUrl;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Override
    public final boolean equals(Object documentEntityObject) {
        if (this == documentEntityObject) {
            return true;
        }
        if (documentEntityObject == null) {
            return false;
        }
        Class<?> documentEntityObjectEffectiveClass =
            documentEntityObject instanceof HibernateProxy
                ? ((HibernateProxy) documentEntityObject).getHibernateLazyInitializer().getPersistentClass()
                : documentEntityObject.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != documentEntityObjectEffectiveClass) {
            return false;
        }
        uk.gov.hmcts.sptribs.document.persistence.DocumentEntity that
            = (uk.gov.hmcts.sptribs.document.persistence.DocumentEntity) documentEntityObject;
        return getId() != null && Objects.equals(getId(), that.getId())
            && getCaseReferenceNumber() != null
            && Objects.equals(getCaseReferenceNumber(), that.getCaseReferenceNumber());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id, caseReferenceNumber);
    }
}

