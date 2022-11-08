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

@Data
@NoArgsConstructor
@Builder
public class HearingDate {

    @CCD(
        displayOrder = 1,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingVenueDate;

    @CCD(
        label = "Session",
        displayOrder = 2,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingSession session;

    @CCD(
        label = "Start time",
        displayOrder = 3,
        regex = "^(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String hearingTime;

    @JsonCreator
    public HearingDate(@JsonProperty("hearingVenueDate") LocalDate hearingVenueDate,
                       @JsonProperty("session") HearingSession session,
                       @JsonProperty("hearingTime") String hearingTime) {
        this.hearingVenueDate = hearingVenueDate;
        this.session = session;
        this.hearingTime = hearingTime;
    }
}
