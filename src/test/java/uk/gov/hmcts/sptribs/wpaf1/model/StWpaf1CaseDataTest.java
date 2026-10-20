package uk.gov.hmcts.sptribs.wpaf1.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.wpaf1.StWpaf1TestUtil.ccdFieldIds;
import static uk.gov.hmcts.sptribs.wpaf1.StWpaf1TestUtil.unwrappedPrefixes;

class StWpaf1CaseDataTest {

    /** CCD's limit on a field id. Exceeding it fails at definition import, not at compile. */
    private static final int CCD_FIELD_ID_LIMIT = 70;

    @Test
    void shouldKeepEveryFieldIdWithinCcdsLimit() {
        final Set<String> ids = ccdFieldIds();

        assertThat(ids)
            .describedAs("the model produced no field ids, so the derivation is broken")
            .isNotEmpty();
        assertThat(ids)
            .describedAs("CCD rejects field ids longer than %d characters; shorten the "
                + "@JsonUnwrapped prefix rather than the question name", CCD_FIELD_ID_LIMIT)
            .allSatisfy(id -> assertThat(id.length()).isLessThanOrEqualTo(CCD_FIELD_ID_LIMIT));
    }

    @Test
    void shouldGiveEverySectionADistinctPrefix() {
        final List<String> prefixes = unwrappedPrefixes();

        assertThat(prefixes)
            .describedAs("two sections sharing a prefix would collide on any question they "
                + "both ask, and the collision is silent")
            .doesNotHaveDuplicates();
    }

    @Test
    void shouldNotLetTheDecisionAndDeclarationPrefixesBeConfused() {
        assertThat(unwrappedPrefixes())
            .describedAs("'dec' for the decision would read as a truncation of the "
                + "declaration's 'decl' in every tab and layout")
            .contains("dsn", "decl")
            .doesNotContain("dec");
    }

    @Test
    void shouldCoverTheWholeOfWpaf1() {
        // Eight numbered sections, the UKSF confidentiality block and the declaration, plus
        // the additional-evidence table. The generated definition has one CaseField more
        // than this: the SDK adds `caseHistory` for the Case History tab.
        assertThat(unwrappedPrefixes()).hasSize(10);
        assertThat(ccdFieldIds()).hasSize(49);
    }

    @Test
    void shouldDefaultEverySectionSoTheCitizenFormCanPatchIntoIt() {
        final StWpaf1CaseData caseData = StWpaf1CaseData.builder().build();

        assertThat(caseData.getAppellant()).isNotNull();
        assertThat(caseData.getRepresentative()).isNotNull();
        assertThat(caseData.getAppointee()).isNotNull();
        assertThat(caseData.getDecision()).isNotNull();
        assertThat(caseData.getReasons()).isNotNull();
        assertThat(caseData.getReconsideration()).isNotNull();
        assertThat(caseData.getTimeliness()).isNotNull();
        assertThat(caseData.getSupport()).isNotNull();
        assertThat(caseData.getUksf()).isNotNull();
        assertThat(caseData.getDeclaration()).isNotNull();
    }
}
