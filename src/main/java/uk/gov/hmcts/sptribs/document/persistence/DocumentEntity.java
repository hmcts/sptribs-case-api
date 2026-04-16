package uk.gov.hmcts.sptribs.document.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "case_documents")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Builder.Default
    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt =  OffsetDateTime.now();

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_filename", nullable = false)
    private String documentFilename;

    @Column(name = "document_binary_url", nullable = false)
    private String documentBinaryUrl;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "is_draft", nullable = false)
    private boolean isDraft;

    @Override
    public final boolean equals(Object documentEntityObject) {
        if (this == documentEntityObject) {
            return true;
        }
        if (documentEntityObject == null) {
            return false;
        }
        Class<?> documentEntityObjectEffectiveClass = documentEntityObject instanceof HibernateProxy
            ? ((HibernateProxy) documentEntityObject).getHibernateLazyInitializer().getPersistentClass() : documentEntityObject.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != documentEntityObjectEffectiveClass) {
            return false;
        }
        DocumentEntity that = (DocumentEntity) documentEntityObject;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

