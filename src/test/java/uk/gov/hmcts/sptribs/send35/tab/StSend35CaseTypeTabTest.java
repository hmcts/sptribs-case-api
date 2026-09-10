package uk.gov.hmcts.sptribs.send35.tab;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.ccd.sdk.api.TabField;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.ccdFieldIds;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.configBuilder;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.tabsFrom;

class StSend35CaseTypeTabTest {

    private final StSend35CaseTypeTab caseTypeTab = new StSend35CaseTypeTab();

    @Test
    void shouldOnlyReferenceFieldsThatExistOnTheCaseData() throws IllegalAccessException {
        final Set<String> known = ccdFieldIds();

        for (Tab<StSend35CaseData, UserRole> tab : configuredTabs()) {
            for (TabField field : tab.getFields()) {
                assertThat(known)
                    .describedAs("tab '%s' shows field '%s', which no question on the case data "
                        + "produces. Tabs address fields by id, so a typo here is invisible to "
                        + "the compiler and shows up as an empty column in XUI.",
                        tab.getTabID(), field.getId())
                    .contains(field.getId());
            }
        }
    }

    @Test
    void shouldShowEveryQuestionSomewhere() throws IllegalAccessException {
        final List<String> shown = configuredTabs().stream()
            .flatMap(tab -> tab.getFields().stream())
            .map(TabField::getId)
            .toList();

        assertThat(shown)
            .describedAs("the whole point of the caseworker view is that the entire appeal is "
                + "readable, so every question must appear on some tab")
            .containsAll(ccdFieldIds());
    }

    @Test
    void shouldGroupTheAppealIntoTheExpectedTabs() throws IllegalAccessException {
        assertThat(configuredTabs())
            .extracting(Tab::getTabID)
            .containsExactly(
                "childOrYoungPerson",
                "appellant",
                "otherParties",
                "communication",
                "typeOfAppeal",
                "schoolOrProvider",
                "reasons",
                "healthAndSocialCare",
                "mediationAndTimeLimits",
                "otherCases",
                "hearingAndSupport",
                "supportingEvidence",
                "declaration");
    }

    @Test
    void shouldGrantEveryTabToTheCaseworker() throws IllegalAccessException {
        assertThat(configuredTabs())
            .allSatisfy(tab -> assertThat(tab.getForRolesAsString()).contains("caseworker"));
    }

    private List<Tab<StSend35CaseData, UserRole>> configuredTabs() throws IllegalAccessException {
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder = configBuilder();
        caseTypeTab.configure(builder);
        return tabsFrom(builder);
    }
}
