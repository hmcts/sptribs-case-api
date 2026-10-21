package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.sptribs.systemupdate.service.FirstHearingDateMigrationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemRemoveFirstHearingDateTaskTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private FirstHearingDateMigrationRepository repository;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private SystemRemoveFirstHearingDateTask task;

    private final CICUser user = new CICUser("test-token", UserInfo.builder().uid("test-user").build());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(task, "caseReference", "");
        ReflectionTestUtils.setField(task, "batchSize", 2);
        ReflectionTestUtils.setField(task, "batchPauseMs", 0L);
    }

    @Test
    void shouldFailClosedWhenDisabled() {
        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(idamService, authTokenGenerator, repository, ccdUpdateService);
    }

    @Test
    void shouldMigrateInKeysetBatchesAndVerifyEveryCase() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));
        when(repository.findNextBatch(20, 2)).thenReturn(List.of(30L));
        when(repository.findNextBatch(30, 2)).thenReturn(List.of());

        task.run();

        var updates = inOrder(ccdUpdateService);
        updates.verify(ccdUpdateService).clearFirstHearingDate(10L, user, "service-token");
        updates.verify(ccdUpdateService).clearFirstHearingDate(20L, user, "service-token");
        updates.verify(ccdUpdateService).clearFirstHearingDate(30L, user, "service-token");
        verify(repository).hasFirstHearingDate(10L);
        verify(repository).hasFirstHearingDate(20L);
        verify(repository).hasFirstHearingDate(30L);
        verify(repository).countRemaining();
    }

    @Test
    void shouldOnlyMigrateSelectedCaseWhenConfigured() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", "12345");
        when(repository.hasFirstHearingDate(12345L)).thenReturn(true, false, false);

        task.run();

        verify(ccdUpdateService).clearFirstHearingDate(12345L, user, "service-token");
        verify(repository, never()).findNextBatch(0, 2);
        verify(repository, never()).countRemaining();
    }

    @Test
    void shouldSkipAnAlreadyClearedSelectedCase() {
        enableTask();
        ReflectionTestUtils.setField(task, "caseReference", "12345");

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldRejectInvalidBatchSizeBeforeAccessingCcd() {
        ReflectionTestUtils.setField(task, "enabled", true);
        ReflectionTestUtils.setField(task, "batchSize", 0);

        assertThatThrownBy(task::run).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(idamService, authTokenGenerator, repository, ccdUpdateService);
    }

    @Test
    void shouldFailWhenCcdDoesNotRemoveTheField() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L));
        when(repository.findNextBatch(10, 2)).thenReturn(List.of());
        when(repository.hasFirstHearingDate(10L)).thenReturn(true);
        when(repository.countRemaining()).thenReturn(1L);

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldContinueAfterFailureAndFailTheRun() {
        enableTask();
        when(repository.findNextBatch(0, 2)).thenReturn(List.of(10L, 20L));
        when(repository.findNextBatch(20, 2)).thenReturn(List.of());
        doThrow(new IllegalStateException("CCD rejected update"))
            .when(ccdUpdateService).clearFirstHearingDate(10L, user, "service-token");

        assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);

        verify(ccdUpdateService).clearFirstHearingDate(20L, user, "service-token");
    }

    private void enableTask() {
        ReflectionTestUtils.setField(task, "enabled", true);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn("service-token");
    }
}
