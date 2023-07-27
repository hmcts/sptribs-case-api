package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.links.LinkService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_MAINTAIN_LINK_CASE;

@ExtendWith(MockitoExtension.class)
class CaseWorkerMaintainLinkCaseTest {
    @InjectMocks
    private CaseWorkerMaintainLinkCase caseWorkerMaintainLinkCase;

    @Mock
    private LinkService linkService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseWorkerMaintainLinkCase.setLinkCaseEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerMaintainLinkCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_MAINTAIN_LINK_CASE);
    }

    @Test
    void shouldNotConfigureMaintainLinkCaseIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerMaintainLinkCase.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_MAINTAIN_LINK_CASE);
    }

    @Test
    void shouldRemoveLinkCase() {
        //Given
        CicCase cicCase = CicCase.builder()
            .linkCaseNumber(TEST_CASE_ID.toString())
            .build();
        CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> start =
            caseWorkerMaintainLinkCase.aboutToStart(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseWorkerMaintainLinkCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submitted = caseWorkerMaintainLinkCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1).isNotNull();
        assertThat(submitted).isNotNull();
        assertThat(start).isNotNull();
    }
}
