package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum FinalDecisionRecipientSubjectCIC implements HasLabel {

    @JsonProperty("SubjectCIC")
    SUBJECT("Subject");

    private final String label;

    public boolean isSubject() {
        return SUBJECT.name().equalsIgnoreCase(this.name());
    }

}
