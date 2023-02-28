package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PageShowConditionsTest {

    @Test
    void shouldSuccessfullyGetIssueDecisionMap() {
        //When
        Map<String, String> result = PageShowConditionsUtil.issueDecisionShowConditions();

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSuccessfullyGetIssueFinalDecisionMap() {
        //When
        Map<String, String> result = PageShowConditionsUtil.issueFinalDecisionShowConditions();

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSuccessfullyGetHearingMap() {
        //When
        Map<String, String> result = PageShowConditionsUtil.editSummaryShowConditions();

        //Then
        assertThat(result).isNotNull();
    }
}
