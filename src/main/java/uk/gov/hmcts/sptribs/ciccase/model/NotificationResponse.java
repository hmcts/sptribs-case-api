package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.*;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDateTime;
import java.util.UUID;

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
        label = "Client Referemce",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String client_reference;

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
