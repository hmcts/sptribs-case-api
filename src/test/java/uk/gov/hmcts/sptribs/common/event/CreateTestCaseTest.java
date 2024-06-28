package uk.gov.hmcts.sptribs.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CreateTestCaseTest {

    @InjectMocks
    private CreateTestCase createTestCase;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        createTestCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("create-test-case");
    }

    @Test
    void shouldMoveCaseIntoChosenStateAndCreateTestCase() throws JsonProcessingException {
        final CaseData caseData =
            CaseData.builder()
                .caseStatus(CaseManagement)
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(mapper.readValue(anyString(), eq(CaseData.class))).thenReturn(caseData());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            createTestCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(CaseManagement);
        assertThat(response.getData().getHyphenatedCaseRef()).isEqualTo(TEST_CASE_ID_HYPHENATED);
    }

    @Test
    void shouldSubmitSupplementaryDataToCcdWhenSubmittedEventTriggered() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseDetails.setId(TEST_CASE_ID);

        createTestCase.submitted(caseDetails, caseDetails);

        verify(ccdSupplementaryDataService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }
}
