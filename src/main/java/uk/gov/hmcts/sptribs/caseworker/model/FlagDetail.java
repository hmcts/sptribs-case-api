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
    private YesNo hearingRelevant;

    @JsonProperty("flagComment")
    private boolean flagComment;

    @JsonProperty("defaultStatus")
    private String defaultStatus;

    @JsonProperty("availableExternally")
    private YesNo availableExternally;

    @JsonProperty("flagCode")
    private String flagCode;

    @JsonProperty("childFlags")
    private List<FlagDetail> childFlags;

    @JsonProperty("listOfValuesLength")
    private int listOfValuesLength;

    @JsonProperty("listOfValues")
    private List<ValueObject> listOfValues;


    @JsonProperty("name_cy")
    private String nameCy;

    @JsonProperty("subTypeKey")
    private String subTypeKey;

    @JsonProperty("subTypeValue")
    private String subTypeValue;

    @JsonProperty("isParent")
    private boolean isParent;

    @JsonProperty("path")
    private List<String> path;


}
