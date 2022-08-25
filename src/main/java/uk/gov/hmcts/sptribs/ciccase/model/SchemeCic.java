package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum SchemeCic implements HasLabel {



    @JsonProperty("Preference")
    Year1996("Date", "1996"),
    Year2001("Date", "2001"),
    Year2008("Date", "2008"),
    Year2012("Date", "2012");
    private String type;
    private final String label;
}
