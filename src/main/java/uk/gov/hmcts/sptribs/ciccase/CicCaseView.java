package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Primary
public class CicCaseView implements CaseView<CaseData, State> {

    private final CorrespondenceRepository correspondenceRepository;

    public CicCaseView(CorrespondenceRepository correspondenceRepository) {
        this.correspondenceRepository = correspondenceRepository;
    }

    @Override
    public CaseData getCase(CaseViewRequest<State> request, CaseData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        List<ListValue<Correspondence>> correspondences = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);

        for (CorrespondenceEntity correspondenceEntity :
            correspondenceRepository.findAllByCaseReferenceNumberOrderBySentOnDesc(request.caseRef())) {
            Document correspondenceDocument = Document.builder()
                .url(correspondenceEntity.getDocumentUrl())
                .filename(correspondenceEntity.getDocumentFilename())
                .binaryUrl(correspondenceEntity.getDocumentBinaryUrl())
                .build();

            Correspondence correspondence = Correspondence.builder()
                .sentOn(correspondenceEntity.getSentOn().toLocalDateTime())
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

        blobCase.setCorrespondence(correspondences);
        return blobCase;
    }
}
