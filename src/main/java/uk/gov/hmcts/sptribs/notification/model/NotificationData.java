package uk.gov.hmcts.sptribs.notification.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationType;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerAccess;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class NotificationData{
    @CCD(
        label = "Notification ID",
        access = {CaseworkerAccess.class}
    )
    private String ID;

    @CCD(
        label = "Reference",
        access = {CaseworkerAccess.class}
    )
    private String reference;

    @CCD(
        label = "Email Address",
        access = {CaseworkerAccess.class}
    )
    private String emailAddress;

    @CCD(
        label = "Notification Type",
        access = {CaseworkerAccess.class}
    )
    private NotificationType notificationType;

    @CCD(
        label = "Notification Status",
        access = {CaseworkerAccess.class}
    )
    private String notificationStatus;

    @CCD(
        label = "Template ID",
        access = {CaseworkerAccess.class}
    )
    private String templateID;

    @CCD(
        label = "Date Sent",
        access = {CaseworkerAccess.class}
    )
    private DateTime sentAt;
}
