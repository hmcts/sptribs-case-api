package uk.gov.hmcts.sptribs.common.event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CaseDataFieldService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_UPDATE_CASE_DATA;

@ExtendWith(MockitoExtension.class)
class UpdateCaseDataEventTest {

    @Mock
    private CaseDataFieldService caseDataFieldService;

    @InjectMocks
    private UpdateCaseDataEvent updateCaseDataEvent;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        updateCaseDataEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_CASE_DATA);
    }

    @Nested
    class MidEvent {

        @Test
        void shouldPassValidationWhenNoIsSelected() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.NO)
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            verify(caseDataFieldService, never()).fieldExists(any(), any());
        }

        @Test
        void shouldPassValidationWhenYesIsSelectedAndFieldExists() {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(YesNo.YES)
                .deleteFieldName("hyphenatedCaseRef")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.fieldExists(eq("hyphenatedCaseRef"), any(CaseData.class)))
                .thenReturn(true);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            verify(caseDataFieldService).fieldExists(eq("hyphenatedCaseRef"), any(CaseData.class));
        }

        @Test
        void shouldFailValidationWhenYesIsSelectedAndFieldDoesNotExist() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("nonExistentField")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.fieldExists(eq("nonExistentField"), any(CaseData.class)))
                .thenReturn(false);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).contains("nonExistentField");
            assertThat(response.getErrors().get(0)).contains("was not found in case data");
        }

        @Test
        void shouldFailValidationWhenYesIsSelectedAndFieldNameIsEmpty() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).isEqualTo("Please enter a field name to delete.");
            verify(caseDataFieldService, never()).fieldExists(any(), any());
        }

        @Test
        void shouldFailValidationWhenYesIsSelectedAndFieldNameIsBlank() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("   ")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).isEqualTo("Please enter a field name to delete.");
        }

        @Test
        void shouldFailValidationWhenYesIsSelectedAndFieldNameIsNull() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName(null)
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).isEqualTo("Please enter a field name to delete.");
        }

        @Test
        void shouldPassValidationWhenDeleteFieldIsNull() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(null)
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            verify(caseDataFieldService, never()).fieldExists(any(), any());
        }

        @Test
        void shouldPassValidationForNestedCicCaseField() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("cicCaseFullName")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.fieldExists(eq("cicCaseFullName"), any(CaseData.class)))
                .thenReturn(true);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldFailValidationForInvalidNestedField() {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("cicCaseInvalidField")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.fieldExists(eq("cicCaseInvalidField"), any(CaseData.class)))
                .thenReturn(false);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.midEvent(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).contains("cicCaseInvalidField");
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldDeleteFieldWhenYesSelectedAndFieldExists() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(YesNo.YES)
                .deleteFieldName("hyphenatedCaseRef")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.deleteField(eq("hyphenatedCaseRef"), any(CaseData.class)))
                .thenReturn(true);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().getDeleteField()).isNull();
            assertThat(response.getData().getDeleteFieldName()).isNull();
            verify(caseDataFieldService).deleteField(eq("hyphenatedCaseRef"), any(CaseData.class));
        }

        @Test
        void shouldNotDeleteFieldWhenNoSelected() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(YesNo.NO)
                .deleteFieldName("hyphenatedCaseRef")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo("1234-5678-9012-3456");
            assertThat(response.getData().getDeleteField()).isNull();
            assertThat(response.getData().getDeleteFieldName()).isNull();
            verify(caseDataFieldService, never()).deleteField(any(), any());
        }

        @Test
        void shouldNotDeleteFieldWhenDeleteFieldIsNull() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(null)
                .deleteFieldName("hyphenatedCaseRef")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo("1234-5678-9012-3456");
            verify(caseDataFieldService, never()).deleteField(any(), any());
        }

        @Test
        void shouldReturnErrorWhenFieldNotFound() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("nonExistentField")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.deleteField(eq("nonExistentField"), any(CaseData.class)))
                .thenReturn(false);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).contains("Failed to delete field 'nonExistentField'");
        }

        @Test
        void shouldReturnErrorWhenExceptionOccurs() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("someField")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.deleteField(eq("someField"), any(CaseData.class)))
                .thenThrow(new IllegalAccessException("Access denied"));

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors().get(0)).contains("Error deleting field 'someField'");
        }

        @Test
        void shouldClearTemporaryFieldsAfterSubmit() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .deleteField(YesNo.YES)
                .deleteFieldName("note")
                .note("Test note to delete")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            when(caseDataFieldService.deleteField(eq("note"), any(CaseData.class)))
                .thenReturn(true);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getData().getDeleteField()).isNull();
            assertThat(response.getData().getDeleteFieldName()).isNull();
        }

        @Test
        void shouldNotAttemptDeleteWhenFieldNameIsBlank() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(YesNo.YES)
                .deleteFieldName("   ")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo("1234-5678-9012-3456");
            verify(caseDataFieldService, never()).deleteField(any(), any());
        }

        @Test
        void shouldNotAttemptDeleteWhenFieldNameIsNull() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .hyphenatedCaseRef("1234-5678-9012-3456")
                .deleteField(YesNo.YES)
                .deleteFieldName(null)
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.CaseManagement);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo("1234-5678-9012-3456");
            verify(caseDataFieldService, never()).deleteField(any(), any());
        }

        @Test
        void shouldPreserveStateAfterDeletion() throws Exception {
            // Given
            final CaseData caseData = CaseData.builder()
                .note("Test note")
                .deleteField(YesNo.YES)
                .deleteFieldName("note")
                .build();

            final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
            caseDetails.setData(caseData);
            caseDetails.setId(TEST_CASE_ID);
            caseDetails.setState(State.AwaitingHearing);

            when(caseDataFieldService.deleteField(eq("note"), any(CaseData.class)))
                .thenReturn(true);

            // When
            final AboutToStartOrSubmitResponse<CaseData, State> response =
                updateCaseDataEvent.aboutToSubmit(caseDetails, caseDetails);

            // Then
            assertThat(response.getState()).isEqualTo(State.AwaitingHearing);
        }
    }
}
