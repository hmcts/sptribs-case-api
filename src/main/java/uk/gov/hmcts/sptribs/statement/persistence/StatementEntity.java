package uk.gov.hmcts.sptribs.statement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.time.OffsetDateTime;

@Entity
@Table(name = "statements")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @Column(name = "case_reference_number", nullable = false)
    private Long caseReferenceNumber;

    @Column(name = "party", nullable = false)
    private String party;

    @Column(name = "uploaded_on", nullable = false)
    private OffsetDateTime uploadedOn;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "document_filename", nullable = false)
    private String documentFilename;

    @Column(name = "document_binary_url", nullable = false)
    private String documentBinaryUrl;
}
