package uk.gov.hmcts.sptribs.common.event;

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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.event.page.ContactPreferenceDetails;
import uk.gov.hmcts.sptribs.common.event.page.SelectParties;
import uk.gov.hmcts.sptribs.launchdarkly.FeatureToggleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CreateTestCaseTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateTestCase createTestCase;


    @InjectMocks
    private SelectParties selectParties;

    @InjectMocks
    private ContactPreferenceDetails contactPreferenceDetails;


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(featureToggleService.isCicCreateCaseFeatureEnabled()).thenReturn(true);
        createTestCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains("create-test-application");
    }


    @Test
    void shouldSelectSubject() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().partiesCIC(Set.of(PartiesCIC.APPLICANT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectParties.midEvent(caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void shouldThrowErrorWhenContactCicCaseIsNull() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .cicCase(null)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectParties.midEvent(caseDetails);

        //Then
        assertThat(response.getErrors().size()).isZero();
    }

    @Test
    void shouldThrowErrorWhenContactPartiesAreNull() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectParties.midEvent(caseDetails);

        //Then
        assertThat(response.getErrors().size()).isNotZero();
    }


    @Test
    void noErrorWhenContactPartiesHasSubjectSelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().partiesCIC(Set.of(PartiesCIC.SUBJECT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectParties.midEvent(caseDetails);

        //Then
        //Then
        assertThat(response.getErrors().size()).isZero();
    }

    @Test
    void shouldSelectContactPreference() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().partiesCIC(Set.of(PartiesCIC.APPLICANT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPreferenceDetails.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }
}
