package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Who the tribunal should send information to — SEND35 question 6.1.
 *
 * <p>The tribunal corresponds with exactly one person for the whole appeal, and it must
 * be someone already named elsewhere on the form.
 */
@Getter
@AllArgsConstructor
public enum StSend35RecipientOfInformation implements HasLabel {

    @JsonProperty("youngPerson")
    YOUNG_PERSON("The young person the appeal is about"),

    @JsonProperty("advocate")
    ADVOCATE("The named advocate"),

    @JsonProperty("representative")
    REPRESENTATIVE("The named representative"),

    @JsonProperty("parentOrCarer")
    PARENT_OR_CARER("The named parent or carer"),

    @JsonProperty("alternativePerson")
    ALTERNATIVE_PERSON("The alternative person");

    private final String label;
}
