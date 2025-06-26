package uk.gov.hmcts.sptribs;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataApi;
import uk.gov.hmcts.sptribs.document.DocAssemblyClient;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingClient;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialClient;
import uk.gov.hmcts.sptribs.recordlisting.LocationClient;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient;
import uk.gov.hmcts.sptribs.systemupdate.service.ScheduledTaskRunner;

import java.util.TimeZone;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.sptribs", "uk.gov.hmcts.reform.ccd.document"}

)
@EnableFeignClients(
    clients = {
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        LocationClient.class,
        JudicialClient.class,
        DocAssemblyClient.class,
        CoreCaseDataApi.class,
        ExtendedCaseDataApi.class,
        CaseAssignmentApi.class,
        CaseDocumentClientApi.class,
        BundlingClient.class,
        TaskManagementClient.class,
    }
)
@EnableScheduling
@EnableRetry
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@Slf4j
public class CaseApiApplication implements CommandLineRunner {

    @Autowired
    ScheduledTaskRunner taskRunner;

    public static void main(final String[] args) {
        final SpringApplication application = new SpringApplication(CaseApiApplication.class);
        final ConfigurableApplicationContext instance = application.run(args);

        if (System.getenv("TASK_NAME") != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv("TASK_NAME") != null) {
            taskRunner.run(System.getenv("TASK_NAME"));
        }
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
    }
}
