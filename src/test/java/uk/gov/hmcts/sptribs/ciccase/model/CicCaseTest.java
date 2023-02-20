package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;

class CicCaseTest {

    @Test
    void shouldRemoveRepresentative() {
        //When
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        Set<ContactPartiesCIC> contactPartiesCICS = new HashSet<>();
        contactPartiesCICS.add(ContactPartiesCIC.REPRESENTATIVETOCONTACT);
        contactPartiesCICS.add(ContactPartiesCIC.RESPONDENTTOCONTACT);
        final CicCase cicCase = CicCase.builder()
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .representativeCIC(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .contactPartiesCIC(contactPartiesCICS)
            .hearingNotificationParties(parties)
            .build();

        //When
        cicCase.removeRepresentative();
        //Then
        assertThat(cicCase.getRepresentativeFullName()).isEmpty();
    }

    @Test
    void shouldRemoveApplicant() {
        //When
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .applicantAddress(APPLICANT_ADDRESS)
            .applicantFullName(APPLICANT_2_FIRST_NAME)
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .applicantCIC(Set.of(ApplicantCIC.APPLICANT_CIC))
            .hearingNotificationParties(parties)
            .build();

        //When
        cicCase.removeApplicant();
        //Then
        assertThat(cicCase.getApplicantFullName()).isEmpty();
    }
}
