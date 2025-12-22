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

    /**
     * Generate an incremental alphabetic sequence based on the number provided.
     * 1 -> AA
     * 2 -> AB
     * 26 -> AZ
     * 52 -> BZ
     * 676 -> ZZ
     * 677 -> AAA
     * @param seq the sequence number
     * @return the generated string
     */
    private String sequenceToString(Long seq) {
        if (seq < 1) {
            throw new IllegalArgumentException("Sequence must be >= 1");
        }

        int length = 2;
        long offset = 0;

        long capacity = 676;

        while (seq > offset + capacity) {
            offset += capacity;
            capacity *= 26;
            length++;
        }

        long position = seq - offset - 1;

        char[] result = new char[length];
        for (int i = length - 1; i >= 0; i--) {
            result[i] = (char) ('A' + (position % 26));
            position /= 26;
        }

        return new String(result);
    }
}
