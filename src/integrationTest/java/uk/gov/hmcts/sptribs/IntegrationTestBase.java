package uk.gov.hmcts.sptribs;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.sptribs.manager.CaseCorrespondenceManager;
import uk.gov.hmcts.sptribs.manager.CaseDataManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentManager;
import uk.gov.hmcts.sptribs.manager.CorrespondenceDocumentManager;
import uk.gov.hmcts.sptribs.manager.IntegrationTestDataManager;

import java.util.List;

@SpringBootTest(properties = "spring.flyway.enabled = true")
@Import({CaseDataManager.class, CaseDocumentManager.class, CorrespondenceDocumentManager.class, CaseCorrespondenceManager.class})
public abstract class IntegrationTestBase {

    @Autowired
    private CaseDataManager caseDataManager;

    @Autowired
    private CaseDocumentManager caseDocumentManager;

    @Autowired
    private CaseCorrespondenceManager caseCorrespondenceManager;

    @Autowired
    private CorrespondenceDocumentManager correspondenceDocumentManager;

    private List<IntegrationTestDataManager> dataManagerList;

    @ServiceConnection
    static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");
        postgreSQLContainer.start();
    }

    @PostConstruct
    void setup() {
        dataManagerList = List.of(caseDataManager, caseDocumentManager, caseCorrespondenceManager, correspondenceDocumentManager);
    }

    @AfterEach
    void cleardown() {
        dataManagerList.forEach(IntegrationTestDataManager::cleanup);
    }
}
