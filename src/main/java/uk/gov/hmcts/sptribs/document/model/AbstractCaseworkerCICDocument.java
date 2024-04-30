package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractCaseworkerCICDocument<D extends CaseworkerCICDocument> {

    private D value;

    @JsonCreator
    public AbstractCaseworkerCICDocument(@JsonProperty("value") D value) {
        this.value = value;
    }
}
