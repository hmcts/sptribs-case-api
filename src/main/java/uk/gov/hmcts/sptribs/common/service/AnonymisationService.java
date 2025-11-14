package uk.gov.hmcts.sptribs.common.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.persistence.AnonymisationEntity;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

@Service
@RequiredArgsConstructor
public class AnonymisationService {

    private final AnonymisationRepository anonymisationRepository;
    private final EntityManager entityManager;

    @Transactional
    public AnonymisationEntity getOrCreateAnonymisation(Long caseReference) {
        return  anonymisationRepository.findByCaseReference(caseReference)
            .orElseGet(() -> {
                AnonymisationEntity entity = anonymisationRepository.saveAndFlush(new AnonymisationEntity(caseReference));
                entityManager.refresh(entity);
                return entity;
            });
    }

    public String generateAnonymisedName(AnonymisationEntity anonymisationEntity) {
        return sequenceToString(anonymisationEntity.getAnonymisationSeq());
    }

    private String sequenceToString(Long seq) {
        StringBuilder stringBuilder = new StringBuilder();
        while (seq > 0) {
            seq--;
            stringBuilder.insert(0, (char) ('A' + (seq % 26)));
            seq /= 26;
        }
        if (stringBuilder.length() == 1) {
            stringBuilder.insert(0, 'A');
        }

        return stringBuilder.toString();
    }
}
