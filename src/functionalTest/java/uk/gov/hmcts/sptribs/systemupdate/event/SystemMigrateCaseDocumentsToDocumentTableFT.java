package uk.gov.hmcts.sptribs.systemupdate.event;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;
import uk.gov.hmcts.sptribs.testutil.data.FunctionalTestDataManager;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
@Slf4j
public class SystemMigrateCaseDocumentsToDocumentTableFT extends FunctionalTestSuite {

    @Autowired
    protected FunctionalTestDataManager functionalTestDataManager;

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-migrate-documents-to-table-about-to-submit.json";

    @Test
    public void shouldMigrateCaseDocumentsOnAboutToSubmit() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, ABOUT_TO_SUBMIT_URL, true);

        Long caseId = functionalTestDataManager.getTestReferences()
            .get(functionalTestDataManager.getTestReferences().size() - 1);

        log.info("Case ID used in test: {}", caseId);
        log.info("Migration response body: {}", response.asString());

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThat(functionalTestDataManager.countCaseDocuments(caseId)).isEqualTo(6);
    }
}
