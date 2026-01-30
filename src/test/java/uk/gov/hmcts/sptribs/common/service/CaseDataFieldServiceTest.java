package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

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
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("cicCaseFullName", caseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFieldDoesNotExist() {
        // Given
        CaseData caseData = CaseData.builder().build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("nonExistentField", caseData)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNestedFieldDoesNotExist() {
        // Given
        CicCase cicCase = CicCase.builder()
            .fullName("Test Name")
            .build();
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        // When/Then
        assertThat(caseDataFieldService.fieldExists("cicCaseInvalidField", caseData)).isFalse();
    }

    @Test
    void shouldDeleteDirectField() throws Exception {
        // Given
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder().build();

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
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder()
            .cicCase(null)
            .build();

        // When/Then - should not throw exception, just return false
        assertThat(caseDataFieldService.fieldExists("cicCaseFullName", caseData)).isFalse();
    }

    @Test
    void shouldHandleDeleteWithNullNestedObject() throws Exception {
        // Given - cicCase is null
        CaseData caseData = CaseData.builder()
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
        CaseData caseData = CaseData.builder().build();

        // When/Then - closeCase fields should be discoverable
        // The prefix "close" maps to closeCase field
        assertThat(caseDataFieldService.getPrefixToFieldMap()).containsKey("close");
    }

    @Test
    void shouldFindFieldInReferToJudge() {
        // Given
        CaseData caseData = CaseData.builder().build();

        // When/Then - referToJudge fields should be discoverable
        assertThat(caseDataFieldService.getPrefixToFieldMap()).containsKey("referToJudge");
    }
}
