package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RootControllerTest {

    @Mock
    private RootController rootController;

    private  AutoCloseable closeableMocks;

    @BeforeEach
    void setUp() {
        closeableMocks = MockitoAnnotations.openMocks(this);
        rootController = new RootController();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeableMocks.close();
    }

    @Test
    void testWelcome() {
        final ResponseEntity<String> response = rootController.welcome();
        assertEquals("Welcome to sptribs-case-api",response.getBody());
    }
}
