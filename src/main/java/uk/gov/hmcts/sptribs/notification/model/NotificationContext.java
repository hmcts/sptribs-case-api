package uk.gov.hmcts.sptribs.notification.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
public class NotificationContext {

    private final CaseData caseData;
    private final String caseReference;
    private final PartiesNotification notification;
    private final Set<NotificationParties> correspondenceParties;
    private final Map<String, String> uploadedDocuments;
    private final CaseData beforeData;

    @Builder.Default
    private final List<String> errors = new ArrayList<>();
    @Builder.Default
    private final List<String> sent = new ArrayList<>();
    @Builder.Default
    private final List<String> correspondenceIDs = new ArrayList<>();

    public static NotificationContext forNotification(CaseData caseData,
                                                      String caseReference,
                                                      PartiesNotification notification) {
        NotificationContext context = builder()
            .caseData(caseData)
            .caseReference(caseReference)
            .notification(notification)
            .build();
        return context.toBuilder()
            .correspondenceParties(notification.buildCorrespondenceParties(context))
            .build();
    }

    public static NotificationContext withBeforeData(CaseData caseData,
                                                     String caseReference,
                                                     PartiesNotification notification,
                                                     CaseData beforeData) {
        NotificationContext context = builder()
            .caseData(caseData)
            .caseReference(caseReference)
            .notification(notification)
            .beforeData(beforeData)
            .build();
        return context.toBuilder()
            .correspondenceParties(notification.buildCorrespondenceParties(context))
            .build();
    }

    public static NotificationContext withUploadedDocuments(CaseData caseData,
                                                            String caseReference,
                                                            PartiesNotification notification,
                                                            Map<String, String> uploadedDocuments) {
        NotificationContext context = builder()
            .caseData(caseData)
            .caseReference(caseReference)
            .notification(notification)
            .uploadedDocuments(uploadedDocuments)
            .build();
        return context.toBuilder()
            .correspondenceParties(notification.buildCorrespondenceParties(context))
            .build();
    }
}
