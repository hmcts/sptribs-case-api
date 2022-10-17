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

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class HearingDate {

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingDateTime;

    @CCD(
        label = "Session",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingSession session;

    @JsonCreator
    public HearingDate(@JsonProperty("hearingDate") LocalDateTime hearingDateTime,
                       @JsonProperty("session") HearingSession session) {
        this.hearingDateTime = hearingDateTime;
        this.session = session;
    }
}
