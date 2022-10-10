package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.organisationPolicy;

class ApplicantTest {

    @Test
    void shouldReturnEnglishIfLanguagePreferenceWelshIsNoOrNull() {

        assertThat(Applicant.builder().languagePreferenceWelsh(NO).build()
            .getLanguagePreference())
            .isEqualTo(ENGLISH);

        assertThat(Applicant.builder().build()
            .getLanguagePreference())
            .isEqualTo(ENGLISH);
    }

    @Test
    void shouldReturnWelshIfLanguagePreferenceWelshIsYes() {

        assertThat(Applicant.builder().languagePreferenceWelsh(YES).build()
            .getLanguagePreference())
            .isEqualTo(WELSH);
    }

    @Test
    void shouldBeRepresentedIfSolicitorIsNotNullAndSolicitorEmailIsNotEmpty() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitor(Solicitor.builder()
                .email("solicitor@example.com")
                .build())
            .solicitorRepresented(YES)
            .build();
        //Then
        assertThat(applicant.isRepresented()).isTrue();
    }

    @Test
    void shouldNotBeRepresentedIfSolicitorIsNullOrSolicitorEmailIsEmpty() {
        //When
        final Applicant applicantNoSolicitor = Applicant.builder().build();

        final Applicant applicantNoSolicitorEmail = Applicant.builder()
            .solicitor(Solicitor.builder().build())
            .build();

        //Then
        assertThat(applicantNoSolicitor.isRepresented()).isFalse();
        assertThat(applicantNoSolicitorEmail.isRepresented()).isFalse();
    }

    @Test
    void shouldReturnTrueIfContactDetailsAreConfidential() {
        //When
        final Applicant applicant = Applicant.builder()
            .contactDetailsType(PRIVATE)
            .build();

        //Then
        assertThat(applicant.isConfidentialContactDetails()).isTrue();
    }

    @Test
    void shouldReturnFalseIfContactDetailsAreNotConfidential() {
        //When
        final Applicant applicant = Applicant.builder()
            .contactDetailsType(PUBLIC)
            .build();
        //Then
        assertThat(applicant.isConfidentialContactDetails()).isFalse();
    }

    @Test
    void shouldReturnFalseIfContactDetailsAreSetToNull() {
        //When
        final Applicant applicant = Applicant.builder()
            .build();
        //Then
        assertThat(applicant.isConfidentialContactDetails()).isFalse();
    }

    @Test
    void shouldReturnTrueIfAppliedForFinancialOrder() {
        //When
        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .build();
        //Then
        assertThat(applicant.appliedForFinancialOrder()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotAppliedForFinancialOrder() {
        //When
        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        //Then
        assertThat(applicant.appliedForFinancialOrder()).isFalse();
    }

    @Test
    void shouldReturnFalseIfAppliedForFinancialOrderIsSetToNull() {
        //When
        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").build())
            .build();

        //Then
        assertThat(applicant1.appliedForFinancialOrder()).isFalse();
        assertThat(applicant2.appliedForFinancialOrder()).isFalse();
    }

    @Test
    void shouldReturnTrueIfNotUkOrUnitedKingdom() {
        //When
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("France").build())
            .build();

        //Then
        assertThat(applicant.isBasedOverseas()).isTrue();
    }

    @Test
    void shouldReturnFalseIfUkOrUnitedKingdom() {
        //When
        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").build())
            .build();

        //Then
        assertThat(applicant1.isBasedOverseas()).isFalse();
        assertThat(applicant2.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnTrueIfScottishAddress() {
        //When
        final Applicant applicant1 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("UK").postCode("KA27 8AB").build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("United Kingdom").postCode("TD11 3AA").build())
            .build();
        final Applicant applicant3 = Applicant.builder()
            .address(AddressGlobalUK.builder().country("Scotland").build())
            .build();

        //Then
        assertThat(applicant1.isBasedOverseas()).isTrue();
        assertThat(applicant2.isBasedOverseas()).isTrue();
        assertThat(applicant3.isBasedOverseas()).isTrue();
    }

    @Test
    void shouldReturnFalseIfGreatBritain() {
        //When
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("Great Britain").build())
            .build();

        //Then
        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnFalseIfGreatBritainAndCaseInsensitive() {
        //When
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("great britain").build())
            .build();

        //Then
        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicantIsRepresentedWhenCheckingIsBasedOverseas() {
        //When
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().country("France").build())
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        //Then
        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnReturnFalseIfAddressNotSet() {
        //When
        final Applicant applicant = Applicant.builder().build();

        //Then
        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnThrowErrorIfCountryIsBlank() {
        //When
        final Applicant applicant = Applicant.builder()
            .address(AddressGlobalUK.builder().build())
            .build();

        //Then
        assertThat(applicant.isBasedOverseas()).isFalse();
    }

    @Test
    void shouldReturnTrueForIs2Offline() {
        assertThat(Applicant.builder().offline(YES).build()
            .isOffline()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsOffline() {
        assertThat(Applicant.builder().offline(NO).build()
            .isOffline()).isFalse();
    }

    @Test
    void shouldReturnSolicitorAddressIfRepresentedWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        //Then
        assertThat(applicant.getCorrespondenceAddress()).isEqualTo("solicitor address");
    }

    @Test
    void shouldReturnSolicitorAddressWithFirmNameIfRepresentedWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .organisationPolicy(organisationPolicy())
                .address("solicitor address")
                .build())
            .build();

        //Then
        assertThat(applicant.getCorrespondenceAddress()).isEqualTo("Test Organisation\nsolicitor address");
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        //Then
        assertThat(applicant.getCorrespondenceAddress())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nPost Code\nUK");
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndAddressIsNullWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .build();

        //Then
        assertThat(applicant.getCorrespondenceAddress()).isNull();
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndPrivateContactDetailsWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        //Then
        assertThat(applicant.getCorrespondenceAddress()).isNull();
    }

    @Test
    void shouldReturnSolicitorAddressIfRepresentedWhenRequestingPostalAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .build())
            .build();

        //Then
        assertThat(applicant.getPostalAddress()).isEqualTo("solicitor address");
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedWhenRequestingPostalAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        //Then
        assertThat(applicant.getPostalAddress())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
    }

    @Test
    void shouldReturnNullIfNotRepresentedAndAddressIsNullWhenRequestingPostalAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .build();

        //Then
        assertThat(applicant.getPostalAddress()).isNull();
    }

    @Test
    void shouldReturnApplicantAddressIfNotRepresentedAndPrivateContactDetailsWhenRequestingCorrespondenceAddress() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        //Then
        assertThat(applicant.getPostalAddress())
            .isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
    }

    @Test
    void shouldSplitAddressLine1WhenItIsMoreThan25CharactersAndAppendToLine2() {
        //When
        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Apartment, Correspondence Street")
                .addressLine2("Line 2,")
                .addressLine3("Line 3,")
                .postTown("Post Town")
                .postCode("Post Code")
                .country("UK")
                .build())
            .build();

        //Then
        assertThat(applicant.getPostalAddress())
            .isEqualTo("Correspondence Apartment\nCorrespondence Street,Line 2,\nLine 3,\nPost Town\nPost Code");
    }

}
