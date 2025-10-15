package uk.gov.hmcts.sptribs.notification.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "correspondences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrespondenceRecord {

    @Id
    private Long id;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "sent_on", insertable = false, updatable = false)
    private OffsetDateTime sentAt;

    @Column(name = "sent_from", nullable = false)
    private String sentFrom;

    @Column(name = "sent_to", nullable = false)
    private String sentTo;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "correspondence_type", nullable = false)
    private String correspondenceType;
}
