package uk.gov.hmcts.sptribs.caseworker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_1;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_2;
import static uk.gov.hmcts.sptribs.ciccase.model.PanelComposition.PANEL_3;
import static uk.gov.hmcts.sptribs.ciccase.model.SecondPanelMember.MEDICAL_MEMBER;
import static uk.gov.hmcts.sptribs.ciccase.model.ThirdPanelMember.LAY_MEMBER;

@ExtendWith(MockitoExtension.class)
public class HearingSummaryTest {

    @Test
    void shouldPopulatePanelCompositionToPanel3WhenThreePanelMembersSet() {
        HearingSummary hearingSummary = new HearingSummary();
        hearingSummary.setPanel1("Tribunal Judge");
        hearingSummary.setPanel2(MEDICAL_MEMBER);
        hearingSummary.setPanel3(LAY_MEMBER);

        hearingSummary.populatePanelComposition();

        assertThat(hearingSummary.getPanelComposition())
            .isEqualTo(PANEL_3);
    }

    @Test
    void shouldPopulatePanelCompositionToPanel2WhenTwoPanelMembersSet() {
        HearingSummary hearingSummary = new HearingSummary();
        hearingSummary.setPanel1("Tribunal Judge");
        hearingSummary.setPanel2(MEDICAL_MEMBER);
        hearingSummary.setPanel3(null);

        hearingSummary.populatePanelComposition();

        assertThat(hearingSummary.getPanelComposition())
            .isEqualTo(PANEL_2);
    }

    @Test
    void shouldPopulatePanelCompositionToPanel1WhenOnlyOnePanelMemberSet() {
        HearingSummary hearingSummary = new HearingSummary();
        hearingSummary.setPanel1("Tribunal Judge");
        hearingSummary.setPanel2(null);
        hearingSummary.setPanel3(null);

        hearingSummary.populatePanelComposition();

        assertThat(hearingSummary.getPanelComposition())
            .isEqualTo(PANEL_1);
    }
}
