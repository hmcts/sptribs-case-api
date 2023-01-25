package uk.gov.hmcts.sptribs.ciccase.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicationType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.MarriageDetails;

import java.time.LocalDate;
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
}
