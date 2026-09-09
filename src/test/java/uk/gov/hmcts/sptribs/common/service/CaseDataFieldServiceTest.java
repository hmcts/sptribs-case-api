package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataFieldServiceTest {

    private CaseDataFieldService caseDataFieldService;

    @BeforeEach
    void setUp() {
        caseDataFieldService = new CaseDataFieldService();
    }

    @Test
    void shouldBuildPrefixMapFromJsonUnwrappedAnnotations() {
        // When
        Map<String, Field> prefixMap = caseDataFieldService.getPrefixToFieldMap();

        // Then
        assertThat(prefixMap).isNotEmpty();
        assertThat(prefixMap).containsKey("cicCase");
        assertThat(prefixMap).containsKey("stay");
        assertThat(prefixMap).containsKey("close");
        assertThat(prefixMap).containsKey("removeStay");
        assertThat(prefixMap).containsKey("referToJudge");
        assertThat(prefixMap).containsKey("referToLegalOfficer");
        assertThat(prefixMap).containsKey("dssCaseData");
    }

    @Test
    void shouldReturnTrueWhenDirectFieldExists() {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("hyphenatedCaseRef", caseData)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenNestedCicCaseFieldExists() {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("Test Name")
            .build();
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("cicCaseFullName", caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFieldDoesNotExist() {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("nonExistentField", caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNestedFieldDoesNotExist() {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("Test Name")
            .build();
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("cicCaseInvalidField", caseData)).isFalse();
    }

    @Test
    void shouldDeleteDirectField() throws Exception {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("hyphenatedCaseRef", caseData);

        // Then
        assertThat(deleted).isTrue();
        assertThat(caseData.getHyphenatedCaseRef()).isNull();
    }

    @Test
    void shouldDeleteNestedCicCaseField() throws Exception {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("Test Name")
            .email("test@test.com")
            .build();
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("cicCaseFullName", caseData);

        // Then
        assertThat(deleted).isTrue();
        assertThat(caseData.getCicCase().getFullName()).isNull();
        assertThat(caseData.getCicCase().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentField() throws Exception {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();

        // When
        boolean deleted = caseDataFieldService.deleteField("nonExistentField", caseData);

        // Then
        assertThat(deleted).isFalse();
    }

    @Test
    void shouldDeleteCicCaseEmailField() throws Exception {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("Test Name")
            .email("test@test.com")
            .build();
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("cicCaseEmail", caseData);

        // Then
        assertThat(deleted).isTrue();
        assertThat(caseData.getCicCase().getEmail()).isNull();
        assertThat(caseData.getCicCase().getFullName()).isEqualTo("Test Name");
    }

    @Test
    void shouldDeleteNoteField() throws Exception {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .note("Test note")
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("note", caseData);

        // Then
        assertThat(deleted).isTrue();
        assertThat(caseData.getNote()).isNull();
    }

    @Test
    void shouldDeleteCaseNumberField() throws Exception {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .caseNumber("12345678")
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("caseNumber", caseData);

        // Then
        assertThat(deleted).isTrue();
        assertThat(caseData.getCaseNumber()).isNull();
    }

    @Test
    void shouldHandleFieldExistsWithNullNestedObject() {
        // Given - cicCase is null by default if not initialized
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(null)
            .build();

        // When/Then - should not throw exception, just return false
        assertThat(caseDataFieldService.fieldExists("cicCaseFullName", caseData)).isFalse();
    }

    @Test
    void shouldHandleDeleteWithNullNestedObject() throws Exception {
        // Given - cicCase is null
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(null)
            .build();

        // When
        boolean deleted = caseDataFieldService.deleteField("cicCaseFullName", caseData);

        // Then
        assertThat(deleted).isFalse();
    }

    @Test
    void shouldFindFieldInCloseCase() {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();

        // When/Then - closeCase fields should be discoverable
        // The prefix "close" maps to closeCase field
        assertThat(caseDataFieldService.getPrefixToFieldMap()).containsKey("close");
    }

    @Test
    void shouldFindFieldInReferToJudge() {
        // Given
        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();

        // When/Then - referToJudge fields should be discoverable
        assertThat(caseDataFieldService.getPrefixToFieldMap()).containsKey("referToJudge");
    }
}
