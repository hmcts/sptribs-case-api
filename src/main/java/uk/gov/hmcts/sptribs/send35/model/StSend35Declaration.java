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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * The declaration and signature.
 *
 * <p>Only the person completing the form on behalf of the child or young person, or a
 * young person appealing alone, can confirm it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Declaration {

    @JsonProperty("Capacity")
    @CCD(
        label = "Declaration",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35DeclarationCapacity",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35DeclarationCapacity capacity;

    @JsonProperty("SignatoryRole")
    @CCD(
        label = "Signed by",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35SignatoryRole",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35SignatoryRole signatoryRole;

    @JsonProperty("FullName")
    @CCD(
        label = "Full name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String fullName;

    @JsonProperty("Signature")
    @CCD(
        label = "Signature",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String signature;

    @JsonProperty("DateSigned")
    @CCD(
        label = "Date signed",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate dateSigned;
}
