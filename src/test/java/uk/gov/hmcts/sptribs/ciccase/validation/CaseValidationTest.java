package uk.gov.hmcts.sptribs.ciccase.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicationType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionTest.CANNOT_EXIST;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionTest.CONNECTION;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.notNull;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateApplicant1BasicCase;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateCaseFieldsForIssueApplication;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateJurisdictionConnections;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.validateMarriageDate;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year ago.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldValidateBasicCase() {
        //Given
        CaseData caseData = new CaseData();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseData.setDivorceOrDissolution(DIVORCE);

        //When
        List<String> errors = validateBasicCase(caseData);

        //Then
        assertThat(errors).hasSize(14);
    }

    @Test
    public void shouldValidateBasicOfflineCase() {
        //Given
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        //When
        List<String> errors = validateBasicCase(caseData);

        //Then
        assertThat(errors).hasSize(11);
    }

    @Test
    public void shouldValidateApplicant1BasicCase() {
        //Given
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");

        //When
        List<String> errors = validateApplicant1BasicCase(caseData);

        //Then
        assertThat(errors).hasSize(8);
    }

    @Test
    public void shouldValidateApplicant1BasicOfflineCase() {
        //Given
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        //When
        List<String> errors = validateApplicant1BasicCase(caseData);

        //Then
        assertThat(errors).hasSize(6);
    }

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        //When
        List<String> response = notNull(null, "field");
        //Then
        assertThat(response).isEqualTo(List.of("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        //When
        List<String> response = validateMarriageDate(LocalDate.now().plus(2, YEARS), "field");
        //Then
        assertThat(response).isEqualTo(List.of("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsOverOneHundredYearsAgo() {
        //Given
        LocalDate oneHundredYearsAndOneDayAgo = LocalDate.now()
            .minus(100, YEARS)
            .minus(1, DAYS);

        //When
        List<String> response = validateMarriageDate(oneHundredYearsAndOneDayAgo, "field");

        //Then
        assertThat(response).isEqualTo(List.of("field" + MORE_THAN_ONE_HUNDRED_YEARS_AGO));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {
        //When
        List<String> response = validateMarriageDate(LocalDate.now().minus(360, DAYS), "field");

        //Then
        assertThat(response).isEqualTo(List.of("field" + LESS_THAN_ONE_YEAR_AGO));
    }

    @Test
    public void shouldReturnTrueWhenCaseHasAwaitingDocuments() {
        //Given
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);

        //When
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);

        //Then
        assertTrue(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnFalseWhenCaseDoesNotHaveAwaitingDocuments() {
        CaseData caseData = new CaseData();
        //When
        caseData.setDivorceOrDissolution(DIVORCE);
        //Then
        assertFalse(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameAndPlaceOfMarriageAreMissing() {
        //Given
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);

        //When
        List<String> errors = validateCaseFieldsForIssueApplication(caseData.getApplication().getMarriageDetails());

        //Then
        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null",
            "PlaceOfMarriage cannot be empty or null"
        );
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameIsMissing() {
        //Given
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");

        //When
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        //Then
        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null"
        );
    }

    @Test
    public void shouldNotReturnErrorWhenBothWhenApp2MarriageCertNameAndPlaceOfMarriageArePresent() {
        //Given
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");
        marriageDetails.setApplicant2Name("TestFname TestMname  TestLname");

        //When
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateJurisdictionConnectionsForCitizenApplication() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(NO)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT));

        //When
        final List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).contains(CONNECTION + APP_1_RESIDENT_JOINT + CANNOT_EXIST);
    }

    @Test
    public void shouldOnlyValidateEmptyJurisdictionConnectionsWhenApplicant1Represented() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Collections.emptySet());

        //When
        List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).containsOnly("JurisdictionConnections" + ValidationUtil.EMPTY);
    }

    @Test
    public void shouldReturnEmptyListForNonEmptyJurisdictionConnectionsWhenApplicant1Represented() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_APP_2_RESIDENT));

        //When
        List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateJurisdictionConnectionsWhenApplicant1IsNotRepresented() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(NO)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT));

        //When
        List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).contains(CONNECTION + APP_1_RESIDENT_JOINT + CANNOT_EXIST);
    }

    @Test
    public void shouldNotReturnErrorsWhenJurisdictionConnectionsIsNotEmptyAndIsPaperCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplication().setNewPaperCase(YES);

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_APP_2_RESIDENT));

        //When
        List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorsWhenJurisdictionConnectionsIsEmptyAndIsPaperCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplication().setNewPaperCase(YES);

        //When
        List<String> errors = validateJurisdictionConnections(caseData);

        //Then
        assertThat(errors).containsExactly("JurisdictionConnections cannot be empty or null");
    }

    @Test
    public void shouldValidateBasicPaperCaseAndReturnNoErrorWhenApplicant2GenderIsNotSet() {
        //Given
        CaseData caseData = new CaseData();
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        caseData.setApplicant2(
            Applicant.builder().email("respondent@test.com").build()
        );

        caseData.getApplication().setNewPaperCase(YES);

        //When
        List<String> errors = validateBasicCase(caseData);

        //Then
        assertThat(errors).hasSize(11);
        assertThat(errors).containsExactly(
            "ApplicationType cannot be empty or null",
            "Applicant1FirstName cannot be empty or null",
            "Applicant1LastName cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null",
            "Applicant1FinancialOrder cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "Applicant1ContactDetailsType cannot be empty or null",
            "Statement of truth must be accepted by the person making the application",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }

    @Test
    public void shouldValidateBasicDigitalCaseAndReturnErrorWhenApplicant2GenderIsNotSet() {
        //Given
        CaseData caseData = new CaseData();
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        caseData.setApplicant2(
            Applicant.builder().email("respondent@test.com").build()
        );

        //When
        List<String> errors = validateBasicCase(caseData);

        //Then
        assertThat(errors).hasSize(12);
        assertThat(errors).containsExactly(
            "ApplicationType cannot be empty or null",
            "Applicant1FirstName cannot be empty or null",
            "Applicant1LastName cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null",
            "Applicant1FinancialOrder cannot be empty or null",
            "Applicant2Gender cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "Applicant1ContactDetailsType cannot be empty or null",
            "Statement of truth must be accepted by the person making the application",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }
}
