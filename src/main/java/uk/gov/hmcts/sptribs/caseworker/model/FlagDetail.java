package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ComplexType(name = "FlagDetail", generate = false)
public class FlagDetail {

    @JsonProperty("name")
    private String name;

    @JsonProperty("subTypeValue")
    private String subTypeValue;

    @JsonProperty("subTypeKey")
    private String subTypeKey;

    @JsonProperty("otherDescription")
    private String otherDescription;

    @JsonProperty("flagComment")
    private String flagComment;

    @JsonProperty("dateTimeModified")
    private LocalDateTime dateTimeModified;

    @JsonProperty("dateTimeCreated")
    private LocalDateTime dateTimeCreated;

    @JsonProperty("path")
    private String path;

    @JsonProperty("hearingRelated")
    private YesOrNo hearingRelated;

    @JsonProperty("flagCode")
    private String flagCode;

    @JsonProperty("status")
    private String status;

}
