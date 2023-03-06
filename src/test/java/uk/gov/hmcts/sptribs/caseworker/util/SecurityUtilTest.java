package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilTest {

    @Test
    void shouldGetStatusForPrivateFalse() {
        //Given
        User user = TestDataHelper.getUser();
        SecurityClass newClass = SecurityClass.PRIVATE;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldGetStatusForPrivateTrue() {
        //Given
        User user = TestDataHelper.getUserWithSeniorJudge();
        SecurityClass newClass = SecurityClass.PRIVATE;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetStatusForRestrictedFalse() {
        //Given
        User user = TestDataHelper.getUser();
        SecurityClass newClass = SecurityClass.RESTRICTED;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldGetStatusForRestrictedTrue() {
        //Given
        User user = TestDataHelper.getUserWithSeniorJudge();
        SecurityClass newClass = SecurityClass.RESTRICTED;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldGetStatusForPublicFalse() {
        //Given
        User user = TestDataHelper.getUser();
        SecurityClass newClass = SecurityClass.PUBLIC;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldGetStatusForPublicTrue() {
        //Given
        User user = TestDataHelper.getUserWithHmctsJudiciary();
        SecurityClass newClass = SecurityClass.PUBLIC;

        //When
        boolean result = SecurityUtil.checkAvailableForNewClass(user, newClass);

        //Then
        assertThat(result).isTrue();
    }
}
