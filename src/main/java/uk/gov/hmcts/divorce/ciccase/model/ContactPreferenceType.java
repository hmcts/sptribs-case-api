package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ContactPreferenceType implements HasLabel {



    @JsonProperty("Preference")
    EMAIL("Email", "Email"),
    POST("Post", "Post");
    private String type;
    private final String label;
}
