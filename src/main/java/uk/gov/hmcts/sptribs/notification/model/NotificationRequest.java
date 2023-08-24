package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Builder
@Getter
public class NotificationRequest {
    private String destinationAddress;
    private String templateId;
    private Map<String, Object> templateVars;
    private boolean hasFileAttachments;
    private Map<String, String> uploadedDocuments;
    private byte [] fileContents;
    private byte [] fileContents1;
    private String reference;
    private String caseId;
}
