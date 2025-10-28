package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.sdk.CaseRepository;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class SptribsCaseRepository implements CaseRepository<CaseData> {

    private final CorrespondenceRepository correspondenceRepository;

    public SptribsCaseRepository(CorrespondenceRepository correspondenceRepository) {
        this.correspondenceRepository = correspondenceRepository;
    }

    @Override
    public CaseData getCase(long caseRef, String state, CaseData data) {

        List<ListValue<Correspondence>> correspondences = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);

        for (CorrespondenceEntity correspondenceEntity : correspondenceRepository.findAllByCaseReferenceNumberOrderBySentOnDesc(caseRef)) {
            Document correspondenceDocument = Document.builder()
                .url(correspondenceEntity.getDocumentUrl())
                .filename(correspondenceEntity.getDocumentFilename())
                .binaryUrl(correspondenceEntity.getDocumentBinaryUrl())
                .build();

            Correspondence correspondence = Correspondence.builder()
                .sentOn(correspondenceEntity.getSentOn() != null ? correspondenceEntity.getSentOn().toLocalDateTime() : null)
                .from(correspondenceEntity.getSentFrom())
                .to(correspondenceEntity.getSentTo())
                .documentUrl(correspondenceDocument)
                .correspondenceType(correspondenceEntity.getCorrespondenceType())
                .build();

            ListValue<Correspondence> correspondenceListValue = new ListValue<>();
            correspondenceListValue.setId(String.valueOf(listValueIndex.incrementAndGet()));
            correspondenceListValue.setValue(correspondence);
            correspondences.add(correspondenceListValue);
        }

        data.setCorrespondence(correspondences);
        return data;
    }
}
