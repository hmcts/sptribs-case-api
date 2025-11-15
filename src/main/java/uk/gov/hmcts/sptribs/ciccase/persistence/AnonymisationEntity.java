package uk.gov.hmcts.sptribs.ciccase.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table (name = "anonymisation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnonymisationEntity {
    @Id
    @Column(name = "case_reference", nullable = false)
    private Long caseReference;

    @Column(name = "anonymisation_seq", nullable = false, updatable = false, insertable = false)
    private Long anonymisationSeq;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public AnonymisationEntity(Long caseReference) {
        this.caseReference = caseReference;
    }
}
