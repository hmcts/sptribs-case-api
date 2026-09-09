package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

/**
 * Who is making the appeal — SEND35 question 2.1.
 *
 * <p>The three routes carry different eligibility rules: a young person appealing alone
 * must be over compulsory school age and under 25, an alternative person appeals on
 * behalf of a young person who cannot appeal themselves, and a parent or carer appeals
 * for a child aged 0 to 16.
 */
@Getter
@AllArgsConstructor
public enum StSend35WhoIsAppealing implements HasLabel {

    @JsonProperty("youngPerson")
    YOUNG_PERSON("I'm appealing for myself as a young person"),

    @JsonProperty("alternativePerson")
    ALTERNATIVE_PERSON("I'm appealing on behalf of the young person as an alternative person"),

    @JsonProperty("parentOrCarer")
    PARENT_OR_CARER("I'm appealing on behalf of the child as a parent or carer");

    private final String label;
}
