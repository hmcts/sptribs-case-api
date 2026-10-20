package uk.gov.hmcts.sptribs.wpaf1.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.wpaf1.event.StWpaf1CloseCase;
import uk.gov.hmcts.sptribs.wpaf1.event.StWpaf1CreateCase;
import uk.gov.hmcts.sptribs.wpaf1.event.StWpaf1RegisterCase;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.wpaf1.StWpaf1TestUtil.configBuilder;

class StWpaf1CaseTypeConfigTest {

    @Test
    void shouldRegisterTheCaseTypeFromTheServiceCode() {
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder = configBuilder();

        new StWpaf1CaseTypeConfig().configure(builder);

        assertThat(CcdServiceCode.ST_WPAF1.getCaseType()).isEqualTo(CcdCaseType.WPAF1);
        assertThat(CcdCaseType.WPAF1.getCaseTypeName()).isEqualTo("StWpaf1");
        assertThat(CcdServiceCode.ST_WPAF1.getJurisdiction())
            .describedAs("WPAF1 stays in ST_CIC: a new jurisdiction needs its own CCD roles, "
                + "XUI_JURISDICTIONS entry and IDAM profile rows")
            .isEqualTo(CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION);
    }

    @Test
    void shouldDeclareTheCitizenAndProgressionEvents() {
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder = configBuilder();

        new StWpaf1CreateCase().configure(builder);
        new StWpaf1RegisterCase().configure(builder);
        new StWpaf1CloseCase().configure(builder);

        final Map<String, Event<StWpaf1CaseData, UserRole, StWpaf1State>> events = getEventsFrom(builder);

        assertThat(events.keySet()).containsExactlyInAnyOrder("create-case", "register-case", "close-case");
        assertThat(events.get("create-case").getPreState())
            .describedAs("create-case starts a new appeal, so it has no pre-state")
            .isEmpty();
        assertThat(events.get("register-case").getPreState()).containsExactly(StWpaf1State.Submitted);
        assertThat(events.get("register-case").getPostState()).containsExactly(StWpaf1State.Registered);
        assertThat(events.get("close-case").getPreState()).containsExactly(StWpaf1State.Registered);
        assertThat(events.get("close-case").getPostState()).containsExactly(StWpaf1State.Closed);
    }

    @Test
    void shouldLetACitizenCreateAnAppeal() {
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder = configBuilder();

        new StWpaf1CreateCase().configure(builder);

        assertThat(getEventsFrom(builder).get("create-case").getGrants().keySet())
            .describedAs("the appeal is submitted by the appellant through the citizen "
                + "prototype, not keyed in by a caseworker")
            .contains(UserRole.CITIZEN);
    }
}
