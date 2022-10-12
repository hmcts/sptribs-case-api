package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class HearingDate {

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hearingDate;

    @CCD(
        label = "Session",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingSession session;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "HH:mm")
    private LocalDateTime startTime;


    @JsonCreator
    public HearingDate(@JsonProperty("hearingDate") LocalDate hearinngDate,
                       @JsonProperty("session") HearingSession session,
                       @JsonProperty("startTime") LocalDateTime startTime) {
        this.hearingDate = hearinngDate;
        this.session = session;
        this.startTime = startTime;
    }
}
