package uk.gov.hmcts.sptribs.common.repositories.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrespondenceDocumentId implements Serializable {

    private Long documentId;
    private UUID correspondenceId;
}
