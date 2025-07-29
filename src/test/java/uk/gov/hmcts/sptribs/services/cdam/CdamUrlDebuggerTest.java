package uk.gov.hmcts.sptribs.services.cdam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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

        assertNotNull(cdamUrlDebugger);
    }
}