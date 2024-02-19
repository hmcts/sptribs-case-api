package uk.gov.hmcts.sptribs.systemupdate.convert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class CaseDetailsListConverterTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CaseDetailsListConverter caseDetailsListConverter;

    @Test
    void shouldOnlyReturnCasesThatDeserializeWhenConverting() {
        //Given
        final List<CaseDetails> caseDetailsList = createCaseDetailsList(10);

        final CaseDetails failedCaseDetails1 = caseDetailsList.get(2);
        final CaseDetails failedCaseDetails2 = caseDetailsList.get(8);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> expectedResult = new ArrayList<>();

        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();

            if (arguments.length > 0) {
                final CaseDetails details = (CaseDetails) arguments[0];
                if (details.equals(failedCaseDetails1) || details.equals(failedCaseDetails2)) {
                    throw new IllegalArgumentException();
                }
            }

            final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
            expectedResult.add(caseDetails);

            return caseDetails;
        }).when(caseDetailsConverter).convertToCaseDetailsFromReformModel(any(CaseDetails.class));

        //When
        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> result =
            caseDetailsListConverter.convertToListOfValidCaseDetails(caseDetailsList);

        //Then
        assertThat(result)
            .hasSize(8)
            .containsAll(expectedResult);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenConverting() {

        final List<CaseDetails> caseDetailsList = createCaseDetailsList(2);

        doThrow(new IllegalArgumentException()).when(caseDetailsConverter).convertToCaseDetailsFromReformModel(any());

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> result =
            caseDetailsListConverter.convertToListOfValidCaseDetails(caseDetailsList);

        assertThat(result).isEmpty();
        verify(caseDetailsConverter, times(2)).convertToCaseDetailsFromReformModel(any());

    }

    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(CaseDetails.class));
        }

        return caseDetails;
    }
}
