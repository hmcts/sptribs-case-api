package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@NoArgsConstructor
@Builder
public class HearingDate {

    @CCD(
        label = "Hearing date",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingVenueDate;

    @CCD(
        label = "Session",
        typeOverride = FixedList,
        typeParameterOverride = "HearingSession",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingSession hearingVenueSession;

    @CCD(
        label = "Start time (24hr format)",
        regex = "^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingVenueTime;

    @JsonCreator
    public HearingDate(@JsonProperty("hearingVenueDate") LocalDate hearingVenueDate,
                       @JsonProperty("session") HearingSession hearingVenueSession,
                       @JsonProperty("hearingTime") String hearingVenueTime) {
        this.hearingVenueDate = hearingVenueDate;
        this.hearingVenueSession = hearingVenueSession;
        this.hearingVenueTime = hearingVenueTime;
    }
}
