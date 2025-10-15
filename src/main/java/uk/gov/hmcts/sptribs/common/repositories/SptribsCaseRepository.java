package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.ccd.sdk.CaseRepository;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceRecord;

import java.util.ArrayList;
import java.util.List;

public class SptribsCaseRepository implements CaseRepository<CaseData> {

    private final CorrespondenceRepository correspondenceRepository;

    public SptribsCaseRepository(CorrespondenceRepository correspondenceRepository) {
        this.correspondenceRepository = correspondenceRepository;
    }

    @Override
    public CaseData getCase(long caseRef, String state, CaseData data) {

        List<Correspondence> correspondences = new ArrayList<>();

        for (CorrespondenceRecord correspondenceRecord : correspondenceRepository.findAllByCaseReferenceNumberOrderBySentAtDesc(caseRef)) {
            Correspondence correspondence = Correspondence.builder()
                .id(correspondenceRecord.getId())
                .caseReferenceNumber(correspondenceRecord.getCaseReferenceNumber())
                .sentOn(correspondenceRecord.getSentAt() != null ? correspondenceRecord.getSentAt().toLocalDateTime() : null)
                .from(correspondenceRecord.getSentFrom())
                .to(correspondenceRecord.getSentTo())
                .documentUrl(correspondenceRecord.getDocumentUrl())
                .build();
            correspondences.add(correspondence);
        }

        data.setCorrespondence(correspondences);
        return data;
    }
}
