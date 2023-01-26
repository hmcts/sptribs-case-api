package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class LabelContent {

    @CCD(label = "applicant 2 or respondent")
    private String applicant2;

    @CCD(label = "applicant 2 or the respondent")
    private String theApplicant2;

    @CCD(label = "Applicant 2 or The respondent")
    private String theApplicant2UC;

    @CCD(label = "Applicant or Respondent")
    private String applicant2UC;

    @CCD(label = "divorce or dissolution")
    private String unionType;

    @CCD(label = "Divorce or Dissolution")
    private String unionTypeUC;

    @CCD(label = "Divorce or civil partnership application")
    private String divorceOrCivilPartnershipApplication;

    @CCD(label = "Divorce or end civil partnership")
    private String divorceOrEndCivilPartnership;

    @CCD(label = "The applicant's or applicant 1’s")
    private String applicantOrApplicant1;

    @CCD(label = "Divorce or civil partnership")
    private String divorceOrCivilPartnership;

    @CCD(label = "Divorce or civil partnership")
    private String finaliseDivorceOrEndCivilPartnership;

    @CCD(label = "marriage or civil partnership")
    private String marriageOrCivilPartnership;

    @CCD(label = "Marriage or civil partnership")
    private String marriageOrCivilPartnershipUC;

    @CCD(label = "Get a divorce or legally end it")
    private String divorceOrLegallyEnd;

    @CCD(label = "applicant's or applicant 1’s")
    private String applicantsOrApplicant1s;

    @CCD(label = "the applicant or applicant 1")
    private String theApplicantOrApplicant1;

    @CCD(label = "The applicant or applicant 1")
    private String theApplicantOrApplicant1UC;

    @CCD(label = "Applicant or Applicant 1")
    private String applicantOrApplicant1UC;

    @CCD(label = "Got married or formed their civil partnership")
    private String gotMarriedOrFormedCivilPartnership;

    @CCD(label = "respondent's or applicant 2’s")
    private String respondentsOrApplicant2s;

}
