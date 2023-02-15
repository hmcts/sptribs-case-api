package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    @CCD(
        label = "Id",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String id;

    @CCD(
        label = "Client Reference",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String clientReference;

    @CCD(
        label = "Notification Type",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NotificationType notificationType;

    @CCD(
        label = "Created at",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDateTime createdAtTime;

    @CCD(
        label = "Updated at",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDateTime updatedAtTime;

    @CCD(
        label = "Status",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String status;
}
