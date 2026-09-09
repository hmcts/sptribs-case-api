package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 13 — whether the appeal is in time.
 *
 * <p>Two months from the date on the local authority decision letter, or one month from
 * the date on the mediation certificate where there is one. A late appeal is reviewed by
 * a judge on the explanation given here.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Timeliness {

    @JsonProperty("DecisionLetterDate")
    @CCD(
        label = "Date printed on the local authority decision letter",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate decisionLetterDate;

    @JsonProperty("MediationCertificateDate")
    @CCD(
        label = "Date printed on the mediation certificate",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate mediationCertificateDate;

    @JsonProperty("LateAppealExplanation")
    @CCD(
        label = "Explanation for appealing after the time limit",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lateAppealExplanation;
}
