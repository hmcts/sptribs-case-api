package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class AbstractCaseworkerCICDocument<D extends CaseworkerCICDocument> {

    private D value;

    @JsonCreator
    public AbstractCaseworkerCICDocument(@JsonProperty("value") D value) {
        this.value = value;
    }
}
