package uk.gov.hmcts.sptribs.caseworker.event;

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
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_PANEL_COMPOSITION;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_3;
import static uk.gov.hmcts.sptribs.ciccase.model.SecondPanelMember.MEDICAL_MEMBER;
import static uk.gov.hmcts.sptribs.ciccase.model.ThirdPanelMember.LAY_MEMBER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CaseworkerPanelCompositionTest {

    @InjectMocks
    private CaseworkerPanelComposition caseworkerPanelComposition;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerPanelComposition.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PANEL_COMPOSITION);
    }

    @Test
    void shouldPopulatePanel1InAboutToStartCallback() {
        final CaseData caseData = new CaseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerPanelComposition.aboutToStart(details);

        assertThat(response.getData().getListing().getSummary().getPanel1())
            .isEqualTo("Tribunal Judge");
    }

    @Test
    void shouldPopulatePanelCompositionInAboutToSubmitCallback() {
        final CaseData caseData = new CaseData();
        caseData.getListing().getSummary().setPanel1("Tribunal Judge");
        caseData.getListing().getSummary().setPanel2(MEDICAL_MEMBER);
        caseData.getListing().getSummary().setPanel3(LAY_MEMBER);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerPanelComposition.aboutToSubmit(details, details);

        assertThat(response.getData().getListing().getSummary().getPanelComposition())
            .isEqualTo(PANEL_3);
    }
}
