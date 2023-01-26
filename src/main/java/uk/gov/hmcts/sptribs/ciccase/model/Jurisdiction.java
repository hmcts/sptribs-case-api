package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.ciccase.model.JurisdictionConnections.RESIDUAL_JURISDICTION_CP;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.CANNOT_EXIST;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.CONNECTION;
import static uk.gov.hmcts.sptribs.ciccase.validation.ValidationUtil.EMPTY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Jurisdiction {

    @CCD(
        label = "Is the applicant resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Residence;

    @CCD(
        label = "Is the respondent resident?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Residence;

    @CCD(
        label = "Is the applicant domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant1Domicile;

    @CCD(
        label = "Is the respondent domiciled?",
        access = {DefaultAccess.class}
    )
    private YesOrNo applicant2Domicile;

    @CCD(
        label = "Has the applicant been resident for the last twelve months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastTwelveMonths;

    @CCD(
        label = "Has the applicant been resident for the last six months?",
        access = {DefaultAccess.class}
    )
    private YesOrNo app1HabituallyResLastSixMonths;

    @CCD(
        label = "Is residual jurisdiction eligible?",
        access = {DefaultAccess.class}
    )
    private YesOrNo residualEligible;

    @CCD(
        label = "Were both the applicant and the respondent last habitually resident, and one still resides?",
        access = {DefaultAccess.class}
    )
    private YesOrNo bothLastHabituallyResident;

    @CCD(
        label = "Legal connections",
        hint = "Tick all the reasons that apply:",
        access = {DefaultAccess.class}
    )
    private Set<JurisdictionConnections> connections;

    public List<String> validateJurisdiction(CaseData data) {
        if (isEmpty(connections)) {
            return List.of("JurisdictionConnections" + EMPTY);
        } else {
            return Stream.of(
                validateJurisdictionConnectionI(data)
            ).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }

    private String validateJurisdictionConnectionI(CaseData data) {
        if (connections.contains(RESIDUAL_JURISDICTION_CP) && data.isDivorce()) {
            return CONNECTION + RESIDUAL_JURISDICTION_CP + CANNOT_EXIST;
        }
        return null;
    }
}
