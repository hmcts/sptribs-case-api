package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * One row of the additional-evidence list the appellant sends with the form.
 *
 * <p>The documents themselves are not uploaded: they are sent to the tribunal separately,
 * so this is the list the caseworker checks the post against.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Evidence {

    @JsonProperty("EvidenceDescription")
    @CCD(
        label = "What is the evidence?",
        hint = "For example: a consultant's report",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String evidenceDescription;

    @JsonProperty("SignedBy")
    @CCD(
        label = "Name of the person who signed or wrote it",
        hint = "For example: Dr M. Smith, Consultant Rheumatologist",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String signedBy;

    @JsonProperty("DocumentDate")
    @CCD(
        label = "Date of document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate documentDate;

    @JsonProperty("PageCount")
    @CCD(
        label = "Number of pages",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String pageCount;
}
