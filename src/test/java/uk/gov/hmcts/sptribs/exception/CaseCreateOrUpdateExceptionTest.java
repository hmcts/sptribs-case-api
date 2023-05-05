package uk.gov.hmcts.sptribs.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CaseCreateOrUpdateExceptionTest {

    @Test
    public void CaseCreateOrUpdateExceptionTestSingle() {
        Assertions.assertThrows(RuntimeException.class,() -> {throw new CaseCreateOrUpdateException("Failing while creating the case");} );
    }
    @Test
    public void CaseCreateOrUpdateExceptionTestDouble() {
        Exception e = new RuntimeException();
        Assertions.assertThrows(RuntimeException.class,() -> {throw new CaseCreateOrUpdateException("Failing while creating the case",e);} );
    }



}
