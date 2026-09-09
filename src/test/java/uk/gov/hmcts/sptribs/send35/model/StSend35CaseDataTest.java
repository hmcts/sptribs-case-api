package uk.gov.hmcts.sptribs.send35.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.ccdFieldIds;
import static uk.gov.hmcts.sptribs.send35.StSend35TestUtil.unwrappedPrefixes;

class StSend35CaseDataTest {

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
    void shouldCoverTheWholeOfSend35() {
        // 17 sections plus the supporting-evidence table. The generated definition has one
        // CaseField more than this: the SDK adds `caseHistory` for the Case History tab.
        assertThat(unwrappedPrefixes()).hasSize(17);
        assertThat(ccdFieldIds()).hasSize(85);
    }

    @Test
    void shouldDefaultEverySectionSoTheCitizenFormCanPatchIntoIt() {
        final StSend35CaseData caseData = StSend35CaseData.builder().build();

        assertThat(caseData.getChildOrYoungPerson()).isNotNull();
        assertThat(caseData.getAppellant()).isNotNull();
        assertThat(caseData.getAdditionalParents()).isNotNull();
        assertThat(caseData.getRepresentative()).isNotNull();
        assertThat(caseData.getAdvocate()).isNotNull();
        assertThat(caseData.getCommunication()).isNotNull();
        assertThat(caseData.getParentalResponsibility()).isNotNull();
        assertThat(caseData.getTypeOfAppeal()).isNotNull();
        assertThat(caseData.getSchoolOrProvider()).isNotNull();
        assertThat(caseData.getReasons()).isNotNull();
        assertThat(caseData.getHealthAndSocialCare()).isNotNull();
        assertThat(caseData.getMediation()).isNotNull();
        assertThat(caseData.getTimeliness()).isNotNull();
        assertThat(caseData.getOtherCases()).isNotNull();
        assertThat(caseData.getHearingPreferences()).isNotNull();
        assertThat(caseData.getSupport()).isNotNull();
        assertThat(caseData.getDeclaration()).isNotNull();
    }
}
