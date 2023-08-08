package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FlagDetail {

    @JsonProperty("name")
    private String name;

    @JsonProperty("hearingRelevant")
    private boolean hearingRelevant;

    @JsonProperty("flagComment")
    private boolean flagComment;

    @JsonProperty("defaultStatus")
    private String defaultStatus;

    @JsonProperty("externallyAvailable")
    private boolean externallyAvailable;

    @JsonProperty("flagCode")
    private String flagCode;

    @JsonProperty("childFlags")
    private List<FlagDetail> childFlags;

    @JsonProperty("listOfValuesLength")
    private int listOfValuesLength;

    @JsonProperty("listOfValues")
    private List<Value> listOfValues;


    @JsonProperty("name_cy")
    private String nameCy;

    @JsonProperty("isParent")
    private boolean isParent;

    @JsonProperty("path")
    private List<String> path;


}
