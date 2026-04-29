package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.exception.CaseEventRepositoryException;
import uk.gov.hmcts.sptribs.common.service.CaseDataRestoreService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateInitialCaseDocuments.SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemMigrateInitialCaseDocumentsTest {

    @InjectMocks
    private SystemMigrateInitialCaseDocuments systemMigrateInitialCaseDocuments;

    @Mock
    private CaseDataRestoreService caseDataRestoreService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        systemMigrateInitialCaseDocuments.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS);
    }

    @Test
    void shouldCallUpdateInitialCaseDocumentsWithCorrectArguments() {
        CaseData caseData = CaseData.builder().build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        systemMigrateInitialCaseDocuments.aboutToSubmit(caseDetails, null);

        verify(caseDataRestoreService).updateInitialCaseDocuments(
            TEST_CASE_ID,
            caseData
        );
    }

    @Test
    void shouldReturnCaseDataInResponse() {
        CaseData caseData = CaseData.builder().build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateInitialCaseDocuments.aboutToSubmit(caseDetails, null);

        CaseData data = response.getData();
        assertThat(data).isEqualTo(caseData);
        assertThat(data.getNewBundleOrderEnabled()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldReturnMutatedCaseDataAfterServiceUpdates() {
        ListValue<CaseworkerCICDocument> doc = ListValue.<CaseworkerCICDocument>builder()
            .id("doc-1")
            .value(CaseworkerCICDocument.builder().build())
            .build();

        CaseData caseData = CaseData.builder().build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        doAnswer(invocation -> {
            CaseData data = invocation.getArgument(1);
            data.setInitialCicaDocuments(List.of(doc));
            return null;
        }).when(caseDataRestoreService).updateInitialCaseDocuments(any(), any());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateInitialCaseDocuments.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getInitialCicaDocuments())
            .hasSize(1)
            .extracting(ListValue::getId)
            .containsExactly("doc-1");
    }

    @Test
    void shouldPropagateExceptionWhenServiceThrows() {
        CaseData caseData = CaseData.builder().build();

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        doThrow(new CaseEventRepositoryException("DB error", new RuntimeException()))
                .when(caseDataRestoreService).updateInitialCaseDocuments(any(), any());

        assertThatThrownBy(() ->
            systemMigrateInitialCaseDocuments.aboutToSubmit(caseDetails, null))
            .isInstanceOf(CaseEventRepositoryException.class)
            .hasMessageContaining("DB error");
    }
}