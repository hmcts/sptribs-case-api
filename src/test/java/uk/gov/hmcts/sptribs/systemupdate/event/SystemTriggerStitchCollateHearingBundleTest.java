package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerStitchCollateHearingBundle.SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemTriggerStitchCollateHearingBundleTest {

    @InjectMocks
    private SystemTriggerStitchCollateHearingBundle systemTriggerStitchCollateHearingBundle;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemTriggerStitchCollateHearingBundle.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE);
    }

    @Test
    void shouldSetStitchHearingBundleTaskToYesInAboutToSubmit() {
        final CaseData caseData = new CaseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemTriggerStitchCollateHearingBundle
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getStitchHearingBundleTask()).isEqualTo(YES);
    }
}
