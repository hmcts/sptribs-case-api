package uk.gov.hmcts.sptribs.notification.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {
    private final ContactPartiesService contactPartiesService;

    public void sendToCorrespondenceParties(NotificationContext notificationContext) {

        notificationContext.getCorrespondenceParties().forEach(party -> {
            try {
                notificationPartiesConsumerMap.get(party).accept(notificationContext);
            } catch (Exception e) {
                log.error("Error when attempting to send notification to {}.", party.getLabel());
                notificationContext.getErrors().add(party.getLabel());
            }
        });
        if (!notificationContext.getCorrespondenceIDs().isEmpty()) {
            contactPartiesService.linkCorrespondenceIdsToDocuments(notificationContext.getCaseData(),
                notificationContext.getUploadedDocuments(), notificationContext.getCorrespondenceIDs());
        }
    }

    private final Map<NotificationParties, Consumer<NotificationContext>> notificationPartiesConsumerMap =
        new EnumMap<>(Map.of(
            NotificationParties.SUBJECT, this::sendToSubject,
            NotificationParties.RESPONDENT, this::sendToRespondent,
            NotificationParties.APPLICANT, this::sendToApplicant,
            NotificationParties.REPRESENTATIVE, this::sendToRepresentative,
            NotificationParties.TRIBUNAL, this::sendToTribunal
        ));

    private void sendToSubject(NotificationContext context) {
        if (context.getUploadedDocuments() != null) {
            context.getCorrespondenceIDs().add(context.getNotification().sendToSubject(
                context.getCaseData(),
                context.getCaseReference(),
                context.getUploadedDocuments()
            ));
        } else {
            context.getNotification().sendToSubject(
                context.getCaseData(),
                context.getCaseReference()
            );
        }
    }

    private void sendToRespondent(NotificationContext context) {
        if (context.getUploadedDocuments() != null) {
            context.getCorrespondenceIDs().add(context.getNotification().sendToRespondent(
                context.getCaseData(),
                context.getCaseReference(),
                context.getUploadedDocuments()
            ));
        } else {
            context.getNotification().sendToRespondent(
                context.getCaseData(),
                context.getCaseReference()
            );
        }
    }

    private void sendToApplicant(NotificationContext context) {
        if (context.getUploadedDocuments() != null) {
            context.getCorrespondenceIDs().add(context.getNotification().sendToApplicant(
                context.getCaseData(),
                context.getCaseReference(),
                context.getUploadedDocuments()
            ));
        } else {
            context.getNotification().sendToApplicant(
                context.getCaseData(),
                context.getCaseReference()
            );
        }
    }

    private void sendToRepresentative(NotificationContext context) {
        if (context.getUploadedDocuments() != null) {
            context.getCorrespondenceIDs().add(context.getNotification().sendToRepresentative(
                context.getCaseData(),
                context.getCaseReference(),
                context.getUploadedDocuments()
            ));
        } else {
            context.getNotification().sendToRepresentative(
                context.getCaseData(),
                context.getCaseReference()
            );
        }
    }

    private void sendToTribunal(NotificationContext context) {
        context.getNotification().sendToTribunal(
            context.getCaseData(),
            context.getCaseReference()
        );

    }}
