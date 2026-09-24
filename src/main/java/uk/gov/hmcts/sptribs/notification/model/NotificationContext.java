package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class NotificationContext {
    private final CaseData caseData;
    private final String caseReference;
    private final Map<String, String> uploadedDocuments;
    private final Set<NotificationParties> correspondenceParties;
    private final PartiesNotification notification;
    private final List<String> correspondenceIDs = new ArrayList<>();
    private final CaseData beforeData;
    private final List<String> errors = new ArrayList<>();
}
