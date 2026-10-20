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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * The declaration and signature.
 *
 * <p>Rule 21(5) requires the notice of appeal to be signed by the appellant, so who signed
 * and in what capacity are part of whether the notice is valid, not presentation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Declaration {

    @JsonProperty("Capacity")
    @CCD(
        label = "Statement of truth",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1DeclarationCapacity",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1DeclarationCapacity capacity;

    @JsonProperty("SignatoryRole")
    @CCD(
        label = "Signed by",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1SignatoryRole",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1SignatoryRole signatoryRole;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateSigned;
}
