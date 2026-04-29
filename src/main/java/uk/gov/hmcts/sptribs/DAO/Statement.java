package uk.gov.hmcts.sptribs.DAO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "statements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statement {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_filename", nullable = false)
    private String documentFilename;

    @Column(name = "document_binary_url", nullable = false)
    private String documentBinaryUrl;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}

