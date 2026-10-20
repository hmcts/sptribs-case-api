package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * The capacity in which the statement of truth is given — WPAF1 declaration.
 *
 * <p>The paper form prints two wordings, one first-person for the appellant and one
 * third-person for a representative or appointee signing on their behalf. Which one was
 * agreed to is a fact about the appeal, so it is stored rather than derived from the
 * signatory's role.
 */
@Getter
@AllArgsConstructor
public enum StWpaf1DeclarationCapacity implements HasLabel {

    @JsonProperty("appellantSigning")
    APPELLANT_SIGNING("I, the appellant, believe that the facts stated in this appeal are true"),

    @JsonProperty("signingOnBehalf")
    SIGNING_ON_BEHALF("The appellant believes the facts stated in this appeal are true, and I am authorised to sign for them");

    private final String label;
}
