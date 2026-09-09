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

/**
 * One row of the supporting-evidence table at the end of SEND35.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Evidence {

    @JsonProperty("EvidenceDescription")
    @CCD(
        label = "What is the evidence?",
        hint = "For example: a doctor's letter",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String evidenceDescription;

    @JsonProperty("SignedBy")
    @CCD(
        label = "Name of the person who signed or wrote it",
        hint = "For example: Dr M. Smith, Paediatrician",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String signedBy;

    @JsonProperty("DocumentDate")
    @CCD(
        label = "Date of document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate documentDate;

    @JsonProperty("PageCount")
    @CCD(
        label = "Number of pages",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String pageCount;
}
