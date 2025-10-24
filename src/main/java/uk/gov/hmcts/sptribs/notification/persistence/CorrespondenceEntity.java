package uk.gov.hmcts.sptribs.notification.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.document.SerializableDocument;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_correspondences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrespondenceEntity {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Column(name = "sent_on", insertable = false, updatable = false)
    private OffsetDateTime sentAt;

    @Column(name = "sent_from", nullable = false)
    private String sentFrom;

    @Column(name = "sent_to", nullable = false)
    private String sentTo;

    @Column(name = "document_url", nullable = false, columnDefinition = "jsonb")
    private SerializableDocument documentUrl;

    @Column(name = "correspondence_type", nullable = false)
    private String correspondenceType;
}
