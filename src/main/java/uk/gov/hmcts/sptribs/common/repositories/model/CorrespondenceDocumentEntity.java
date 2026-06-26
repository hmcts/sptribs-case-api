package uk.gov.hmcts.sptribs.common.repositories.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@IdClass(CorrespondenceDocumentId.class)
@Table(name = "correspondence_document")
public class CorrespondenceDocumentEntity {

    @Id
    private Long documentId;

    @Id
    private UUID correspondenceId;
}
