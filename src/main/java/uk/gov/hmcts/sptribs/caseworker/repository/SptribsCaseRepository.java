package uk.gov.hmcts.sptribs.caseworker.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseRepository;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.persistence.NotificationRecord;
import uk.gov.hmcts.sptribs.notification.persistence.NotificationsRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class SptribsCaseRepository implements CaseRepository<CaseData> {


    @Autowired
    private NotificationsRepository notificationsRepository;

    @Override
    public CaseData getCase(long caseRef, String state, CaseData data) {
        List<NotificationRecord> notifications =
            notificationsRepository.findAllByCaseReferenceOrderBySentAtDesc(caseRef);

        data.setNotificationsMarkdown(renderNotificationsMarkdown(notifications));
        return data;
    }

    private String renderNotificationsMarkdown(List<NotificationRecord> records) {
        StringBuilder markdown = new StringBuilder("### Notifications\n\n");

        if (records == null || records.isEmpty()) {
            markdown.append("_No notifications recorded yet._");
            return markdown.toString();
        }

        final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.UK)
                .withZone(ZoneId.of("Europe/London"));

        records.forEach(record -> {
            markdown.append("- ");
            markdown.append(Optional.ofNullable(record.getRecipient())
                .filter(value -> !value.isBlank())
                .orElse("n/a"));

            if (record.getSentAt() != null) {
                markdown.append(" (sent at ");
                markdown.append(displayFormatter.format(record.getSentAt()));
                markdown.append(')');
            }

            markdown.append('\n');
        });

        return markdown.toString();
    }
}
