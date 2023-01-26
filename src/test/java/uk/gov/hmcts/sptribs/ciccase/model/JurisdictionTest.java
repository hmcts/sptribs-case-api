package uk.gov.hmcts.sptribs.ciccase.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.EMPTY;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

public class JurisdictionTest {

    public static final String CONNECTION = "Connection ";
    public static final String CANNOT_EXIST = " cannot exist";

    @Test
    public void shouldNotReturnErrorsWhenJurisdictionConnectionIsCAndIForValidCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_SOLE, JurisdictionConnections.RESIDUAL_JURISDICTION_CP));

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        Assertions.assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsCForJointCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_SOLE));

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_RESIDENT_SOLE + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsC2ForSoleCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_2_RESIDENT_JOINT));

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_2_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsJForSoleCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_RESIDENT_JOINT));

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        assertThat(errors, contains(CONNECTION + JurisdictionConnections.APP_1_RESIDENT_JOINT + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionIsIForDivorceCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setConnections(Set.of(JurisdictionConnections.RESIDUAL_JURISDICTION_CP));

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        assertThat(errors, contains(CONNECTION + JurisdictionConnections.RESIDUAL_JURISDICTION_CP + CANNOT_EXIST));
    }

    @Test
    public void shouldReturnErrorWhenJurisdictionConnectionsIsNull() {
        //Given
        final CaseData caseData = caseData();
        Jurisdiction jurisdiction = new Jurisdiction();

        //When
        List<String> errors = jurisdiction.validateJurisdiction(caseData);

        //Then
        assertThat(errors, contains("JurisdictionConnections" + EMPTY));
    }
}
