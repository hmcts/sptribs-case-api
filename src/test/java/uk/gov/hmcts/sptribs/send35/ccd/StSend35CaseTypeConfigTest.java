package uk.gov.hmcts.sptribs.send35.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdCaseType;
import uk.gov.hmcts.sptribs.common.ccd.CcdJurisdiction;
import uk.gov.hmcts.sptribs.common.ccd.CcdServiceCode;
import uk.gov.hmcts.sptribs.send35.event.StSend35CloseCase;
import uk.gov.hmcts.sptribs.send35.event.StSend35CreateCase;
import uk.gov.hmcts.sptribs.send35.event.StSend35RegisterCase;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.configBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

class StSend35CaseTypeConfigTest {

    @Test
    void shouldRegisterTheCaseTypeFromTheServiceCode() {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configBuilder();

        new StSend35CaseTypeConfig().configure(builder);

        assertThat(CcdServiceCode.ST_SEND35.getCaseType()).isEqualTo(CcdCaseType.SEND35);
        assertThat(CcdCaseType.SEND35.getCaseTypeName()).isEqualTo("StSend35");
        assertThat(CcdServiceCode.ST_SEND35.getJurisdiction())
            .describedAs("SEND stays in ST_CIC: a new jurisdiction needs its own CCD roles, "
                + "XUI_JURISDICTIONS entry and IDAM profile rows")
            .isEqualTo(CcdJurisdiction.CRIMINAL_INJURIES_COMPENSATION);
    }

    @Test
    void shouldDeclareTheCitizenAndProgressionEvents() {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configBuilder();

        new StSend35CreateCase().configure(builder);
        new StSend35RegisterCase().configure(builder);
        new StSend35CloseCase().configure(builder);

        final Map<String, Event<StSend35CaseData, UserRole, StSend35State>> events = getEventsFrom(builder);

        assertThat(events.keySet()).containsExactlyInAnyOrder("create-case", "register-case", "close-case");
        assertThat(events.get("create-case").getPreState())
            .describedAs("create-case starts a new appeal, so it has no pre-state")
            .isEmpty();
        assertThat(events.get("register-case").getPreState()).containsExactly(StSend35State.Submitted);
        assertThat(events.get("register-case").getPostState()).containsExactly(StSend35State.Registered);
        assertThat(events.get("close-case").getPreState()).containsExactly(StSend35State.Registered);
        assertThat(events.get("close-case").getPostState()).containsExactly(StSend35State.Closed);
    }

    @Test
    void shouldLetACitizenCreateAnAppeal() {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configBuilder();

        new StSend35CreateCase().configure(builder);

        assertThat(getEventsFrom(builder).get("create-case").getGrants().keySet())
            .describedAs("the appeal is submitted by the appellant through the citizen "
                + "prototype, not keyed in by a caseworker")
            .contains(UserRole.CITIZEN);
    }
}
