package uk.gov.hmcts.sptribs.cftlib.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class Wiremock {
    private static final WireMockServer server = new WireMockServer(options()
        .port(8765)
        .usingFilesUnderClasspath("wiremock"));

    @PostConstruct
    void init() {
        server.start();
        System.out.println("Wiremock started on port " + server.port());
    }

    public static void stopAndReset() {
        if (server.isRunning()) {
            server.stop();
            server.resetAll();
        }
    }
}
