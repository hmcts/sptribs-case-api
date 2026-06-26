package uk.gov.hmcts.sptribs;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest
@Import({CaseDataManager.class, CaseDocumentManager.class})
public abstract class IntegrationTestBase {

    @Autowired
    private CaseDataManager caseDataManager;

    @Autowired
    private CaseDocumentManager caseDocumentManager;

    private List<IntegrationTestDataManager> dataManagerList;

    @ServiceConnection
    static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");
        postgreSQLContainer.start();
    }

    @PostConstruct
    void setup() {
        dataManagerList = List.of(caseDataManager, caseDocumentManager);
    }

    @AfterEach
    void cleardown() {
        dataManagerList.forEach(IntegrationTestDataManager::cleanup);
    }
}
