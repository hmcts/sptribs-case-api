package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymisationServiceTest {

    @InjectMocks
    private AnonymisationService anonymisationService;

    @Mock
    private AnonymisationRepository anonymisationRepository;

    @Test
    void shouldGenerateUniqueAnonymisedName() {
        String anonymisedName1 = anonymisationService.getOrCreateAnonymisation();
        String anonymisedName2 = anonymisationService.getOrCreateAnonymisation();

        assertNotNull(anonymisedName1);
        assertNotNull(anonymisedName2);
        assertNotEquals(anonymisedName1, anonymisedName2, "Anonymised names should be unique");
    }

    @ParameterizedTest
    @CsvSource({
            "1, AA",
            "2, AB",
            "3, AC",
            "26, AZ",
            "27, BA",
            "28, BB",
            "52, BZ",
            "53, CA",
            "676, ZZ",
            "677, AAA",
            "702, AAZ",
            "703, ABA",
            "704, ABB",
            "705, ABC",
            "728, ABZ",
            "729, ACA",
            "18252, ZZZ",
            "18253, AAAA",
            "18278, AAAZ",
            "18279, AABA",
            "18280, AABB"
    })
    void shouldReturnExpectedSequenceToString(long input, String expected) {

        when(anonymisationRepository.getNextSequenceValue()).thenReturn(input);

        String anonymisedString = anonymisationService.getOrCreateAnonymisation();

        assertThat(anonymisedString).isEqualTo(expected);
    }
}