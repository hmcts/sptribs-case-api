package uk.gov.hmcts.sptribs;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.sptribs.manager.CaseCorrespondenceITManager;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentITManager;
import uk.gov.hmcts.sptribs.manager.CorrespondenceDocumentITManager;
import uk.gov.hmcts.sptribs.manager.IntegrationTestDataManager;

import java.util.List;

@SpringBootTest(properties = "spring.flyway.enabled = true")
@Import({CaseDataITManager.class, CaseDocumentITManager.class, CorrespondenceDocumentITManager.class, CaseCorrespondenceITManager.class})
public abstract class IntegrationTestBase {

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private CaseDocumentITManager caseDocumentITManager;

    @Autowired
    private CaseCorrespondenceITManager caseCorrespondenceITManager;

    @Autowired
    private CorrespondenceDocumentITManager correspondenceDocumentITManager;

    private List<IntegrationTestDataManager> dataManagerList;

    @ServiceConnection
    static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");
        postgreSQLContainer.start();
    }

    @PostConstruct
    void setup() {
        dataManagerList = List.of(caseDataITManager, caseDocumentITManager, caseCorrespondenceITManager, correspondenceDocumentITManager);
    }

    @AfterEach
    void cleardown() {
        dataManagerList.forEach(IntegrationTestDataManager::cleanup);
    }
}
