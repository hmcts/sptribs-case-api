package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymisationServiceTest {

    @InjectMocks
    private AnonymisationService anonymisationService;

    @Mock
    private AnonymisationRepository anonymisationRepository;

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

    @Test
    void shouldApplyAnonymitySelectionYesAndNoName() {
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .build();
        List<String> errors = new ArrayList<>();

        when(anonymisationRepository.getNextSequenceValue()).thenReturn(1L);

        anonymisationService.applyAnonymitySelection(cicCase, errors, true);

        assertThat(cicCase.getAnonymisedAppellantName()).isEqualTo("AA");
        assertThat(cicCase.getAnonymisationDate()).isEqualTo(LocalDate.now());
        assertThat(cicCase.getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldApplyAnonymitySelectionNo() {
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.NO)
            .anonymisationDate(LocalDate.now())
            .anonymityAlreadyApplied(YesOrNo.YES)
            .build();
        List<String> errors = new ArrayList<>();

        anonymisationService.applyAnonymitySelection(cicCase, errors, true);

        assertThat(cicCase.getAnonymisationDate()).isNull();
        assertThat(cicCase.getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldProcessAnonymityFlagWhenCicCaseIsNull() {
        // Given
        CaseData caseData = CaseData.builder().build();
        List<String> errors = new ArrayList<>();

        // When
        anonymisationService.processAnonymityFlag(caseData, null, errors);

        // Then
        assertThat(caseData.getCicCase()).isNotNull();
        assertThat(caseData.getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldSetAnonymiseYesWhenActiveAnonymityFlagExists() {
        // Given
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().build())
            .build();

        List<ListValue<FlagDetail>> flags = new ArrayList<>();
        flags.add(buildAnonymityFlag("1", "Active"));
        caseData.setCaseFlags(Flags.builder().details(flags).build());

        List<String> errors = new ArrayList<>();
        when(anonymisationRepository.getNextSequenceValue()).thenReturn(2L);

        // When
        anonymisationService.processAnonymityFlag(caseData, null, errors);

        // Then
        assertThat(caseData.getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getCicCase().getAnonymisedAppellantName()).isEqualTo("AB");
        assertThat(caseData.getCicCase().getAnonymisationDate()).isEqualTo(LocalDate.now());
        assertThat(caseData.getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldSetAnonymiseNoWhenNoActiveAnonymityFlagExists() {
        // Given
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AB")
                .anonymisationDate(LocalDate.now())
                .anonymityAlreadyApplied(YesOrNo.YES)
                .build())
            .build();

        List<ListValue<FlagDetail>> flags = new ArrayList<>();
        flags.add(buildAnonymityFlag("1", "Inactive"));
        caseData.setCaseFlags(Flags.builder().details(flags).build());

        List<String> errors = new ArrayList<>();

        // When
        anonymisationService.processAnonymityFlag(caseData, null, errors);

        // Then
        assertThat(caseData.getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
        assertThat(caseData.getCicCase().getAnonymisedAppellantName()).isEqualTo("AB"); // keeps name
        assertThat(caseData.getCicCase().getAnonymisationDate()).isNull();
        assertThat(caseData.getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
        assertThat(errors).isEmpty();
    }

    private ListValue<FlagDetail> buildAnonymityFlag(String id, String status) {
        return ListValue.<FlagDetail>builder()
            .id(id)
            .value(FlagDetail.builder()
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .status(status)
                .dateTimeCreated(LocalDateTime.now())
                .build())
            .build();
    }
}