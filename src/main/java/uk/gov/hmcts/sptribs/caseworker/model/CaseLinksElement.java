package uk.gov.hmcts.sptribs.caseworker.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseLinksElement<T>  {
    @JsonProperty("id")
    private final String id;
    @NotNull
    @Valid
    @JsonProperty("value")
    private final T value;
}
