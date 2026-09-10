package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 12 — the mediation certificate.
 *
 * <p>Required in most cases. A Section I-only appeal is exempt; any other reason is for a
 * judge to accept, so an unexplained absence delays the appeal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Mediation {

    @JsonProperty("HasCertificate")
    @CCD(
        label = "Do you have a mediation certificate?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasCertificate;

    @JsonProperty("NoCertificateReason")
    @CCD(
        label = "Why you do not have a mediation certificate",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35NoMediationReason",
        showCondition = "medHasCertificate=\"No\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35NoMediationReason noCertificateReason;

    @JsonProperty("NoCertificateExplanation")
    @CCD(
        label = "Explanation of why you do not have a mediation certificate",
        typeOverride = TextArea,
        showCondition = "medNoCertificateReason=\"otherReason\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String noCertificateExplanation;
}
