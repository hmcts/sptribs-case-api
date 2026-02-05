package uk.gov.hmcts.sptribs.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PublicHolidaysService {
    @Value("${bankholiday.url:https://www.gov.uk/bank-holidays.json}")
    private String holidayUrl;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Set<LocalDate>> holidaysByRegion = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadAllHolidays() {
        try {
            JsonNode root = mapper.readTree(new URL(holidayUrl));

            loadRegion(root, "england-and-wales");
            loadRegion(root, "scotland");
            loadRegion(root, "northern-ireland");

            log.info("Loaded bank holidays for {} regions", holidaysByRegion.size());
        } catch (IOException e) {
            log.error("Failed to load bank holidays", e);
        }
    }

    private void loadRegion(JsonNode root, String region) {
        JsonNode events = root.get(region).get("events");
        Set<LocalDate> holidays = new HashSet<>();

        events.forEach(event -> {
            holidays.add(LocalDate.parse(event.get("date").asText()));
        });

        holidaysByRegion.put(region, Collections.unmodifiableSet(holidays));
    }

//    @Scheduled(cron = "0 8 1 * *") // Refresh on the 1st of every month at 08:00 AM
    @Scheduled(cron = "*/2 * * * *") // For testing: Refresh every 2 minutes
    @SchedulerLock(
            name = "refreshBankHolidays",
            lockAtMostFor = "5m",
            lockAtLeastFor = "1m"
    )
    public void refreshHolidays() {
        log.info("Refreshing bank holidays from scheduled job");
        loadAllHolidays();
    }

    public boolean isBankHoliday(LocalDate date, String region) {
        Set<LocalDate> holidays = holidaysByRegion.getOrDefault(region, Collections.emptySet());
        return holidays.contains(date);
    }
}
