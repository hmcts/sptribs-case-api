package uk.gov.hmcts.sptribs.ciccase.util;

import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AccessCodeGeneratorTest {

    @Test
    void shouldBe8CharsLength() {

        assertThat(AccessCodeGenerator.generateAccessCode().length(), is(8));
    }

    @Test
    void shouldBeAlphaNumeric() {

        assertTrue(AccessCodeGenerator.generateAccessCode().matches("^[a-zA-Z2-9]*$"));
    }
}
