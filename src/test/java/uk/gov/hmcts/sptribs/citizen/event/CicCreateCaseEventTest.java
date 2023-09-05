package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CicCreateCaseEventTest {

    @InjectMocks
    private CicCreateCaseEvent cicCreateCaseEvent;

    @Test
    void shouldChangeCaseStateWhenAboutToSubmit() {
        // Given
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        details.setId(TEST_CASE_ID);
        CaseData caseData = caseData();
        details.setData(caseData);

        // When
        AboutToStartOrSubmitResponse<CaseData, State> response = cicCreateCaseEvent.aboutToSubmit(
            details,
            beforeDetails
        );

        // Then
        assertThat(response.getState()).isEqualTo(State.DSS_Draft);
    }
}
