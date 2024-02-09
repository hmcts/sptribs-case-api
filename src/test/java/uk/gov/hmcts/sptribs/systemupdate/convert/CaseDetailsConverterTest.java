package uk.gov.hmcts.sptribs.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CaseDetailsConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetails caseDetails;

    @InjectMocks
    private CaseDetailsConverter caseDetailsConverter;

    @Test
    void shouldConvertToCaseDetailsFromReformModel() {

        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();
        caseData.setCicCase(cicCase);

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> expectedResult =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        expectedResult.setData(caseData);
        expectedResult.setId(TEST_CASE_ID);
        expectedResult.setCreatedDate(LOCAL_DATE_TIME);

        doReturn(expectedResult)
            .when(objectMapper)
            .convertValue(any(CaseDetails.class), any(TypeReference.class));

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> result =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(caseDetails);

        assertNotNull(result);
        assertEquals(expectedResult, result);
        assertEquals(expectedResult.getData(),result.getData());
        assertEquals(expectedResult.getId(),result.getId());
        assertEquals(expectedResult.getCreatedDate(),result.getCreatedDate());
    }

    @Test
    void shouldConvertToNullCaseDetailsFromReformModel() {

        doReturn(null)
            .when(objectMapper)
            .convertValue(any(), any(TypeReference.class));

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> result =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(null);

        assertNull(result);
        verify(objectMapper).convertValue(eq(null), any(TypeReference.class));
    }
}

