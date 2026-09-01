package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CicCreateCaseEventTest {

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @InjectMocks
    private CicCreateCaseEvent cicCreateCaseEvent;

    @Test
    void shouldChangeCaseStateWhenAboutToSubmit() {
        final CaseDetails<CriminalInjuriesCompensationData, State> details = new CaseDetails<>();
        final CaseDetails<CriminalInjuriesCompensationData, State> beforeDetails = new CaseDetails<>();

        details.setId(TEST_CASE_ID);
        final CriminalInjuriesCompensationData caseData = caseData();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = cicCreateCaseEvent.aboutToSubmit(
            details,
            beforeDetails
            );

        assertThat(response.getState()).isEqualTo(State.DSS_Draft);
    }

    @Test
    void shouldSubmitSupplementaryDataToCcdWhenSubmittedEventTriggered() {
        final CriminalInjuriesCompensationData caseData = caseData();

        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(DSS_Draft);
        caseDetails.setId(TEST_CASE_ID);

        cicCreateCaseEvent.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataRequestToCcd(TEST_CASE_ID.toString());
    }
}
