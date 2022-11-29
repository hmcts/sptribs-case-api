package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.sptribs.notification.EmailTemplateName;

import java.util.Map;

@Builder
@Getter
public class NotificationRequest {
    private String destinationAddress;
    private EmailTemplateName template;
    private Map<String, Object> templateVars;
    private byte [] fileContents;
    private boolean hasEmailAttachment;
}
