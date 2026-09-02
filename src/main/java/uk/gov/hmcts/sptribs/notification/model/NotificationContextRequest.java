package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.util.Map;

@Value
@Builder
public class NotificationContextRequest {
    CaseData caseData;
    String caseReference;
    PartiesNotification notification;
    Map<String, String> uploadedDocuments;
    CaseData previousCaseData;
}
