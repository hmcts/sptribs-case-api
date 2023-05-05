package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CaseCreateOrUpdateExceptionTest {

    @Test
    public void shouldCaseCreateOrUpdateExceptionTestSingle() {
        Assertions.assertThrows(RuntimeException.class, ()
            -> {
            throw new CaseCreateOrUpdateException("Failing while creating the case");
        });
    }

    @Test
    public void shouldCaseCreateOrUpdateExceptionTestDouble() {
        Exception e = new RuntimeException();
        Assertions.assertThrows(RuntimeException.class, ()
            -> {
            throw new CaseCreateOrUpdateException("Failing while creating the case", e);
        });
    }


}
