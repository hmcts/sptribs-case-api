package uk.gov.hmcts.sptribs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataClientAutoConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.sptribs.document.DocAssemblyClient;
import uk.gov.hmcts.sptribs.document.DocumentManagementClient;
import uk.gov.hmcts.sptribs.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.sptribs.payment.PaymentPbaClient;
import uk.gov.hmcts.sptribs.recordlisting.LocationClient;
import uk.gov.hmcts.sptribs.systemupdate.service.ScheduledTaskRunner;

import java.util.TimeZone;
import javax.annotation.PostConstruct;

@SpringBootApplication(
    exclude = {CoreCaseDataClientAutoConfiguration.class},
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.sptribs"}
)
@EnableFeignClients(
    clients = {
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        FeesAndPaymentsClient.class,
        LocationClient.class,
        DocAssemblyClient.class,
        CoreCaseDataApi.class,
        CaseAssignmentApi.class,
        DocumentManagementClient.class,
        PaymentPbaClient.class
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
        final var application = new SpringApplication(CaseApiApplication.class);
        final var instance = application.run(args);

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
