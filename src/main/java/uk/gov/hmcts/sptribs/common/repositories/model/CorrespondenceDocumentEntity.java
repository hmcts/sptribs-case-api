package uk.gov.hmcts.sptribs.common.repositories.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "correspondence_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrespondenceDocumentEntity {

    @EmbeddedId
    private CorrespondenceDocumentId id;

}
