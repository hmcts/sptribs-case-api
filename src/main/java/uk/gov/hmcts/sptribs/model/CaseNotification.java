package uk.gov.hmcts.sptribs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseNotification {
    @CCD(
        label = "Date Sent",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @CCD(
        label = "Reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String reference;

    @CCD(
        label = "Email Address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;

    @CCD(
        label = "Notification Type",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationType notificationType;

    @CCD(
        label = "Notification Status",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String status;

    @CCD(
        label = "Template ID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String templateId;

    @CCD(
        label = "Party",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String party;
}
