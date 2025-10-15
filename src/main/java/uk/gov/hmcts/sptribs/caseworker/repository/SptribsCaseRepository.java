package uk.gov.hmcts.sptribs.caseworker.repository;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseRepository;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.notification.persistence.NotificationRecord;
import uk.gov.hmcts.sptribs.notification.persistence.NotificationsRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
public class SptribsCaseRepository implements CaseRepository<CaseData> {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.UK)
            .withZone(ZoneId.of("Europe/London"));

    private final NotificationsRepository notificationsRepository;
    private final Template notificationsTemplate;

    public SptribsCaseRepository(NotificationsRepository notificationsRepository,
                                 Mustache.Compiler mustacheCompiler) {
        this.notificationsRepository = notificationsRepository;
        this.notificationsTemplate = loadTemplate(mustacheCompiler);
    }

    @Override
    public CaseData getCase(long caseRef, String state, CaseData data) {
        List<NotificationRecord> notifications =
            notificationsRepository.findAllByCaseReferenceOrderBySentAtDesc(caseRef);

        data.setNotificationsMarkdown(renderNotificationsMarkdown(notifications));
        return data;
    }

    private Template loadTemplate(Mustache.Compiler mustacheCompiler) {
        ClassPathResource resource = new ClassPathResource("templates/notifications.mustache");
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return mustacheCompiler.compile(reader);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load notifications.mustache", exception);
        }
    }

    private String renderNotificationsMarkdown(List<NotificationRecord> records) {
        List<NotificationRow> notifications = records == null
            ? Collections.emptyList()
            : records.stream()
                .map(this::toTemplateModel)
                .toList();

        HashMap<String, Object> model = new HashMap<>();
        model.put("notifications", notifications);

        notifications.stream()
            .map(NotificationRow::sentAt)
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(latest -> model.put("latest", latest));

        return notificationsTemplate.execute(model);
    }

    private NotificationRow toTemplateModel(NotificationRecord record) {
        String recipient = Optional.ofNullable(record.getRecipient())
            .filter(value -> !value.isBlank())
            .orElse("n/a");

        String sentAt = record.getSentAt() != null
            ? DISPLAY_FORMATTER.format(record.getSentAt())
            : null;
        return new NotificationRow(recipient, sentAt);
    }

    private record NotificationRow(String recipient, String sentAt) { }
}
