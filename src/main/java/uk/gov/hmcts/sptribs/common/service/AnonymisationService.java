package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

@Service
@RequiredArgsConstructor
public class AnonymisationService {

    private final AnonymisationRepository anonymisationRepository;

    public String getOrCreateAnonymisation() {
        Long sequence = anonymisationRepository.getNextSequenceValue();
        return sequenceToString(sequence);
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
