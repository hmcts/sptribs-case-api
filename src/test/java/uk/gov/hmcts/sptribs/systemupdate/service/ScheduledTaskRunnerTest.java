package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskRunnerTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private Runnable task;

    @InjectMocks
    private ScheduledTaskRunner taskRunner;

    @Test
    void shouldFindTheBean() {
        //Given
        when(context.getBean("lowerCaseBean")).thenReturn(task);
        //When
        taskRunner.run("LowerCaseBean");
        //Then
        verify(task).run();
    }

    @Test
    void shouldNotFindTheBean() {
        //Given
        when(context.getBean("missingBean")).thenThrow();
        //When
        taskRunner.run("missingBean");
        //Then
        verifyNoInteractions(task);
    }
}
