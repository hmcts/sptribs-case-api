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
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDetailsConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetails caseDetails;

    @InjectMocks
    private CaseDetailsConverter caseDetailsConverter;

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertToCaseDetailsFromReformModel() {

        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> expectedResult =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        when(objectMapper.convertValue(any(), any(TypeReference.class))).thenReturn(expectedResult);

        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> result =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(caseDetails);

        assertEquals(expectedResult, result);
    }
}

