package uk.gov.hmcts.sptribs.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.sptribs.notification.TemplateName;

import java.util.Map;

@Builder
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
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
