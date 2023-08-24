package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class NotificationResponse {
    private String templateId;
    private String reference;
}
