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
import uk.gov.hmcts.sptribs.caseworker.service.FlagService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getAppellantFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRespondentFlags;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_MANAGE_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerManageCaseFlagTest {

    @InjectMocks
    private CaseworkerManageCaseFlag caseworkerManageCaseFlag;

    @Mock
    FlagService flagService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseworkerManageCaseFlag.setCaseFlagsEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerManageCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_MANAGE_CASE_FLAG);
    }

    @Test
    void shouldNotConfigureManageCaseFlagIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerManageCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_MANAGE_CASE_FLAG);
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();

        CicCase cicCase = CicCase.builder()
            .appellantFlags(getAppellantFlags())
            .respondentFlags(getRespondentFlags())
            .caseFlags(getCaseFlags())
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
    }

}
