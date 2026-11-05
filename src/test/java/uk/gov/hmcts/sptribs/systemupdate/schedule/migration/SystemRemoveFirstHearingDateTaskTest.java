package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.SystemEventAction;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutionContext;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutor;
import uk.gov.hmcts.sptribs.systemupdate.service.FirstHearingDateMigrationRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult.Outcome.EXECUTED;
import static uk.gov.hmcts.ccd.sdk.SystemEventExecutionResult.Outcome.REPLAYED;

@ExtendWith(MockitoExtension.class)
class SystemRemoveFirstHearingDateTaskTest {

    @Mock
    private FirstHearingDateMigrationRepository repository;

    @Mock
    private ObjectProvider<SystemEventExecutor> systemEventExecutor;

    @Mock
    private SystemEventExecutor executor;

    @InjectMocks
    private SystemRemoveFirstHearingDateTask task;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(task, "caseReference", "");
        ReflectionTestUtils.setField(task, "batchSize", 2);
        ReflectionTestUtils.setField(task, "batchPauseMs", 0L);
    }

    @Test
    void shouldFailClosedWhenDisabled() {
        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(systemEventExecutor, executor, repository);
    }

    @Test
    void shouldMigrateInKeysetBatchesAndVerifyEveryCase() {
        enableTask();
        executeActions();
        when(repository.removeFirstHearingDate(anyLong())).thenReturn(true);
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));
        when(repository.findNextBatch(20, 2)).thenReturn(List.of(30L));
        when(repository.findNextBatch(30, 2)).thenReturn(List.of());

        task.run();

        var updates = inOrder(executor);
        updates.verify(executor).execute(org.mockito.ArgumentMatchers.eq(10L), any(UUID.class), any());
        updates.verify(executor).execute(org.mockito.ArgumentMatchers.eq(20L), any(UUID.class), any());
        updates.verify(executor).execute(org.mockito.ArgumentMatchers.eq(30L), any(UUID.class), any());
        verify(repository).removeFirstHearingDate(10L);
        verify(repository).removeFirstHearingDate(20L);
        verify(repository).removeFirstHearingDate(30L);
        verify(repository).countRemaining();
    }

    @Test
    void shouldOnlyMigrateSelectedCaseWhenConfigured() {
        enableTask();
        executeActions();
        ReflectionTestUtils.setField(task, "caseReference", " 12345 ");
        when(repository.hasFirstHearingDate(12345L)).thenReturn(true, false, false);
        when(repository.removeFirstHearingDate(12345L)).thenReturn(true);

        task.run();

        verify(repository).removeFirstHearingDate(12345L);
        verify(repository, never()).findNextBatch(0, 2);
        verify(repository, never()).countRemaining();
    }

    @Test
    void shouldSkipAnAlreadyClearedSelectedCase() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", "12345");

        task.run();

        verifyNoInteractions(executor);
    }

    @Test
    void shouldProcessAllCasesWhenReferenceIsNull() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", null);

        task.run();

        verify(repository).findNextBatch(0, 2);
        verify(repository).countRemaining();
    }

    @Test
    void shouldProcessAllCasesWhenReferenceIsBlank() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", "  ");

        task.run();

        verify(repository).findNextBatch(0, 2);
        verify(repository).countRemaining();
    }

    @Test
    void shouldRejectInvalidBatchSizeBeforeAccessingExecutor() {
        ReflectionTestUtils.setField(task, "enabled", true);
        ReflectionTestUtils.setField(task, "batchSize", 0);

        assertThatThrownBy(task::run).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(systemEventExecutor, executor, repository);
    }

    @Test
    void shouldRejectNegativeBatchPauseBeforeAccessingExecutor() {
        ReflectionTestUtils.setField(task, "enabled", true);
        ReflectionTestUtils.setField(task, "batchPauseMs", -1L);

        assertThatThrownBy(task::run).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("First hearing date removal batch pause must not be negative");

        verifyNoInteractions(systemEventExecutor, executor, repository);
    }

    @Test
    void shouldFailWhenSelectedCaseStillHasFirstHearingDate() {
        enableTask();
        executeActions();
        ReflectionTestUtils.setField(task, "caseReference", "12345");
        when(repository.hasFirstHearingDate(12345L)).thenReturn(true);
        when(repository.removeFirstHearingDate(12345L)).thenReturn(true);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("removal incomplete");

        verify(repository).removeFirstHearingDate(12345L);
    }

    @Test
    void shouldFailWhenSelectedCaseGainsFirstHearingDateAfterSelection() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", "12345");
        when(repository.hasFirstHearingDate(12345L)).thenReturn(false, true);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("removal incomplete");

        verifyNoInteractions(executor);
    }

    @Test
    void shouldFailWhenEventDoesNotRemoveTheField() {
        enableTask();
        executeActions();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());
        when(repository.removeFirstHearingDate(10L)).thenReturn(true);
        when(repository.hasFirstHearingDate(10L)).thenReturn(true);
        when(repository.countRemaining()).thenReturn(1L);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailWhenCasesRemainEvenIfEveryProcessedCaseSucceeded() {
        enableTask();
        when(repository.countRemaining()).thenReturn(1L);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("removal incomplete");
    }

    @Test
    void shouldFailWhenCaseTypeDoesNotMatch() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());
        doAnswer(invocation -> {
            SystemEventAction action = invocation.getArgument(2);
            action.execute(new SystemEventExecutionContext(10L, invocation.getArgument(1), "OtherCaseType", "Submitted"));
            return new SystemEventExecutionResult(42L, EXECUTED);
        }).when(executor).execute(anyLong(), any(UUID.class), any());

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("removal incomplete");

        verify(repository, never()).removeFirstHearingDate(anyLong());
    }

    @Test
    void shouldSkipCaseClearedSinceSelectionWithoutCreatingAnEvent() {
        enableTask();
        executeActions();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());

        task.run();

        verify(repository).removeFirstHearingDate(10L);
        verify(repository).hasFirstHearingDate(10L);
    }

    @Test
    void shouldFailWhenCaseIsStillPresentAfterConcurrentRemoval() {
        enableTask();
        executeActions();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());
        when(repository.hasFirstHearingDate(10L)).thenReturn(true);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("removal incomplete");

        verify(repository).removeFirstHearingDate(10L);
    }

    @Test
    void shouldTreatReplayAsSuccessOnlyWhenTheKeyIsAbsent() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());
        when(executor.execute(anyLong(), any(UUID.class), any()))
            .thenReturn(new SystemEventExecutionResult(42L, REPLAYED));

        task.run();

        verify(repository, never()).removeFirstHearingDate(anyLong());
        verify(repository).hasFirstHearingDate(10L);
    }

    @Test
    void shouldContinueAfterFailureAndFailTheRun() {
        enableTask();
        executeActions();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));
        when(repository.findNextBatch(20, 2)).thenReturn(List.of());
        when(repository.removeFirstHearingDate(10L)).thenThrow(new IllegalStateException("Update failed"));
        when(repository.removeFirstHearingDate(20L)).thenReturn(true);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);

        verify(repository).removeFirstHearingDate(20L);
    }

    @Test
    void shouldRestoreInterruptStatusAndStopBetweenBatches() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));

        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class)
                .hasMessage("First hearing date removal interrupted")
                .hasCauseInstanceOf(InterruptedException.class);
        } finally {
            assertThat(Thread.interrupted()).isTrue();
        }

        verify(repository, never()).findNextBatch(20, 2);
    }

    @Test
    void shouldUseStableAndDistinctIdempotencyKeys() {
        enableTask();
        executeActions();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));
        when(repository.findNextBatch(20, 2)).thenReturn(List.of());
        when(repository.removeFirstHearingDate(anyLong())).thenReturn(true);

        task.run();

        var keys = org.mockito.ArgumentCaptor.forClass(UUID.class);
        verify(executor, times(2)).execute(anyLong(), keys.capture(), any());
        assertThat(keys.getAllValues()).doesNotHaveDuplicates();
    }

    private void enableTask() {
        ReflectionTestUtils.setField(task, "enabled", true);
        when(systemEventExecutor.getObject()).thenReturn(executor);
    }

    private void executeActions() {
        doAnswer(invocation -> {
            long reference = invocation.getArgument(0);
            UUID key = invocation.getArgument(1);
            SystemEventAction action = invocation.getArgument(2);
            var result = action.execute(new SystemEventExecutionContext(
                reference, key, "CriminalInjuriesCompensation", "Submitted"));
            assertThat(result.state()).isEmpty();
            return new SystemEventExecutionResult(42L, EXECUTED);
        }).when(executor).execute(anyLong(), any(UUID.class), any());
    }
}
