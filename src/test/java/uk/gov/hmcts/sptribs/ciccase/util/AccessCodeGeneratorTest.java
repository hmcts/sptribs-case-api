package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AccessCodeGeneratorTest {

    @Test
    public void shouldBe8CharsLength() {

        assertThat(AccessCodeGenerator.generateAccessCode().length(), is(8));
    }

    @Test
    public void shouldBeAlphaNumeric() {

        assertTrue(AccessCodeGenerator.generateAccessCode().matches("^[a-zA-Z2-9]*$"));
    }
}
