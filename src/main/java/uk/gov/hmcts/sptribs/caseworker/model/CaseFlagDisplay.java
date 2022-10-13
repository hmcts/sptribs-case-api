package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CaseFlagDisplay {

    @CCD(
        label = "Name"
    )
    private String name;

    @CCD(
        label = "FlagType"
    )
    private String flagType;

    @CCD(
        label = "Comments"
    )
    private String comments;

    @CCD(
        label = "Creation date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")

    private LocalDateTime creationDate;
    @CCD(
        label = "Last modified"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastModified;

    @CCD(
        label = "Status")

    private String status;

    public CaseFlagDisplay(String name, String flagType, String comments, LocalDateTime creationDate,
                           LocalDateTime lastModified, String status) {
        this.name = name;
        this.flagType = flagType;
        this.comments = comments;
        this.creationDate = creationDate;
        this.lastModified = lastModified;
        this.status = status;
    }

}
