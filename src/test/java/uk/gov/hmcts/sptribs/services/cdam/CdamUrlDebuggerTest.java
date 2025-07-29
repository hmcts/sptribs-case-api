package uk.gov.hmcts.sptribs.services.cdam;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CdamUrlDebuggerTest {

    @InjectMocks
    private CdamUrlDebugger cdamUrlDebugger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLogUrls() {
        cdamUrlDebugger.logUrls();

        Assertions.assertNotNull(cdamUrlDebugger);
    }
}