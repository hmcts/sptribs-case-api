package uk.gov.hmcts.sptribs.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RootControllerTest {

    @Mock
    private RootController rootController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rootController = new RootController();
    }

    @Test
    void testWelcome() throws Exception {
        ResponseEntity<String> response = rootController.welcome();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Welcome to sptribs-case-api",response.getBody());
    }
}
