package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.AddSystemUpdateRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CicCreateCaseEventTest {

    @InjectMocks
    private CicCreateCaseEvent cicCreateCaseEvent;

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));

        AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent("citizen-cic-create-dss-application");

        cicAppDetail.setEventIds(eventsConfig);

    }

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
        assertThat(response.getState()).isEqualTo(State.DSS_Submitted);
        assertThat(response.getData().getHyphenatedCaseRef()).isNotNull();
    }
}
