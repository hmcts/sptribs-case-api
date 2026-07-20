package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.FAILED_TO_ANONYMISE_CASE;

@Service
@RequiredArgsConstructor
public class AnonymisationService {

    private final AnonymisationRepository anonymisationRepository;

    public String getOrCreateAnonymisation() {
        Long sequence = anonymisationRepository.getNextSequenceValue();
        return sequenceToString(sequence);
    }

    public void applyAnonymitySelection(CicCase cicCase, List<String> errors, boolean updateAnonymityAlreadyApplied) {
        if (YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo())) {
            if (cicCase.getAnonymisedAppellantName() == null) {
                String anonymisedName = getOrCreateAnonymisation();

                if (anonymisedName == null) {
                    errors.add(FAILED_TO_ANONYMISE_CASE);
                    return;
                }
                cicCase.setAnonymisedAppellantName(anonymisedName);
            }
            if (cicCase.getAnonymisationDate() == null) {
                cicCase.setAnonymisationDate(LocalDate.now());
            }
            if (updateAnonymityAlreadyApplied) {
                cicCase.setAnonymityAlreadyApplied(YesOrNo.YES);
            }
            return;
        }

        if (YesOrNo.NO.equals(cicCase.getAnonymiseYesOrNo())) {
            cicCase.setAnonymisationDate(null);
            if (updateAnonymityAlreadyApplied) {
                cicCase.setAnonymityAlreadyApplied(YesOrNo.NO);
            }
        }
    }

    public void processAnonymityFlag(CaseData caseData, CaseData beforeData, List<String> errors) {
        CicCase cicCase = caseData.getCicCase();
        if (cicCase == null) {
            cicCase = new CicCase();
            caseData.setCicCase(cicCase);
        }

        ListValue<FlagDetail> effectiveAnonymityFlag = CaseFlagsUtil.mergeAnonymityFlagsPreserveOriginalId(caseData, beforeData);
        boolean hasActiveAnonymityFlag = CaseFlagsUtil.isActiveFlag(effectiveAnonymityFlag);
        cicCase.setAnonymiseYesOrNo(hasActiveAnonymityFlag ? YesOrNo.YES : YesOrNo.NO);
        applyAnonymitySelection(cicCase, errors, true);
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
