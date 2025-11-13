package uk.gov.hmcts.sptribs.common.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.persistence.AnonymisationEntity;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

@Service
@RequiredArgsConstructor
public class AnonymisationService {

    private final AnonymisationRepository anonymisationRepository;

    @Transactional
    public AnonymisationEntity getOrCreateAnonymisation(Long caseReference) {
        return anonymisationRepository.findByCaseReference(caseReference)
                .orElseGet(() -> anonymisationRepository.saveAndFlush(new AnonymisationEntity()));
    }

    public String generateAnonymisedName(AnonymisationEntity anonymisationEntity) {
        return sequenceToString(anonymisationEntity.getAnonymisationSeq());
    }

    private String sequenceToString(Long seq) {
        StringBuilder stringBuilder = new StringBuilder();
        while (seq > 0) {
            seq --;
            stringBuilder.insert(0, (char) ('A' + (seq % 26)));
            seq /= 26;
        }
        return stringBuilder.toString();
    }
}
