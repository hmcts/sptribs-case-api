package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum OrderTemplate implements HasLabel {
    @JsonProperty("GeneralDirections")
    GENERALDIRECTIONS("General Directions"),

    @JsonProperty("Medical Evidence - DMI Reports")
    DMIREPORTS("Medical Evidence - DMI Reports"),

    @JsonProperty("Medical Evidence - joint instruction")
    JOINTINSTRUCTION("Medical Evidence - joint instruction"),

    @JsonProperty("Police Evidence")
    POLICEEVIDENCE("Police Evidence"),

    @JsonProperty("Strike out warning Directions Notice")
    STRIKEOUTWARNINGDIRECTIONSNOTICE("Strike out warning Directions Notice"),

    @JsonProperty("Pro forma summons")
    PROFORMASUMMONS("Pro forma summons");



    private final String label;

}
