package uk.gov.hmcts.sptribs.document.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "document_download_status",
    uniqueConstraints = @UniqueConstraint(name = "uq_doc_download_party", columnNames = {"document_id", "party"})
)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDownloadStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Column(name = "document_id", nullable = false)
    private long documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "party", nullable = false, columnDefinition = "party")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Party party;

    @Column(name = "downloaded_at")
    private OffsetDateTime downloadedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> otherEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != otherEffectiveClass) {
            return false;
        }
        DocumentDownloadStatusEntity that = (DocumentDownloadStatusEntity) o;
        return getId() != 0 && getId() == that.getId();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
