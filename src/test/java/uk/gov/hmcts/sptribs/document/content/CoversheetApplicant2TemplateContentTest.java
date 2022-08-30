package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.formatId;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CoversheetApplicant2TemplateContentTest {

    @InjectMocks
    private CoversheetApplicant2TemplateContent coversheetApplicant2TemplateContent;

    @Test
    void shouldReturnCoversheetTemplateContent() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplicant2().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line 1")
                .postCode("postcode")
                .build()
        );

        //When
        final Map<String, Object> result = coversheetApplicant2TemplateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(1616591401473378L));
        expectedEntries.put("applicantFirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicantLastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put("applicantAddress", "line 1\npostcode");

        //Then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
