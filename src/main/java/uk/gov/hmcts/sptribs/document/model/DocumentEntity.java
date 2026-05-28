package uk.gov.hmcts.sptribs.document.model;

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

import java.time.OffsetDateTime;

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

    @Column(name = "sent_to_applicant_via_contact_parties", nullable = false)
    private boolean sentToApplicantViaContactParties;

    @Override
    public boolean equals(Object documentEntityObject) {
        if (documentEntityObject == null || getClass() != documentEntityObject.getClass()) {
            return false;
        }

        DocumentEntity that = (DocumentEntity) documentEntityObject;
        return getId() == that.getId()
            && isDraft() == that.isDraft()
            && isSentToApplicantViaContactParties() == that.isSentToApplicantViaContactParties()
            && getCaseReferenceNumber().equals(that.getCaseReferenceNumber())
            && getSavedAt().equals(that.getSavedAt())
            && getDocumentUrl().equals(that.getDocumentUrl())
            && getDocumentFilename().equals(that.getDocumentFilename())
            && getDocumentBinaryUrl().equals(that.getDocumentBinaryUrl())
            && getCategoryId().equals(that.getCategoryId());
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getCaseReferenceNumber().hashCode();
        result = 31 * result + getSavedAt().hashCode();
        result = 31 * result + getDocumentUrl().hashCode();
        result = 31 * result + getDocumentFilename().hashCode();
        result = 31 * result + getDocumentBinaryUrl().hashCode();
        result = 31 * result + getCategoryId().hashCode();
        result = 31 * result + Boolean.hashCode(isDraft());
        result = 31 * result + Boolean.hashCode(isSentToApplicantViaContactParties());
        return result;
    }
}

