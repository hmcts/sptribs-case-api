package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.SchemeCic;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
public class FinalDecisionTemplateContentTest {


    @InjectMocks
    private FinalDecisionTemplateContent templateContent;

    private static final LocalDate ISSUE_DATE = LocalDate.of(2022, 2, 2);

    @Test
    public void shouldSuccessfullyApplyFinalDecisionContent() {
        //Given
        CaseData caseData = buildCaseData();

        //When
        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(result).contains(
            entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel())
        );
    }

    private CaseData buildCaseData() {
        final CicCase cicCase = CicCase.builder().schemeCic(SchemeCic.Year1996).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
