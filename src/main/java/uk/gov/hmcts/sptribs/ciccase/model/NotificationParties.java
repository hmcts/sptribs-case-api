package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum NotificationParties implements HasLabel {

    @JsonProperty("Subject")
    SUBJECT("Subject"),

    @JsonProperty("Representative")
    REPRESENTATIVE("Representative"),

    @JsonProperty("Applicant")
    APPLICANT("Applicant"),

    @JsonProperty("Respondent")
    RESPONDENT("Respondant");

    private final String label;

}
