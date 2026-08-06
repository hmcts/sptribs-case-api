package uk.gov.hmcts.sptribs.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.APPLICATION;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.DECISION;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.FINAL_DECISION;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.ORDER;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemMigrateCaseDocumentsToDocumentTableFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-migrate-documents-to-table-about-to-submit.json";

    private final Map<String, String> listOfTestBinarys;

    {
        String binaryURL = "http://dm-store-aat.service.core-compute-aat.internal/documents/";
        listOfTestBinarys = Map.of(
            ORDER.name(), binaryURL + "f54afe26-172e-44ad-be48-bd68ad53ffbc/binary",
            DOCUMENT_MANAGEMENT.name(), binaryURL + "7c6ca230-c5ee-42ac-96c4-e340b110808e/binary",
            DRAFT_ORDER.name(), binaryURL + "50d3cdd5-20ce-47b7-ae9e-7a798849c19b/binary",
            DECISION.name(), binaryURL + "8ccefd85-a0b1-4acb-bc90-6ce676a3b7a4/binary",
            APPLICATION.name(), binaryURL + "f045922d-d08b-4864-a5f3-3145911f0086/binary",
            FINAL_DECISION.name(), binaryURL + "d4748cf4-de21-4e49-8d1e-f5cb2971e2d5/binary");
    }

    @Test
    public void shouldMigrateCaseDocumentsOnAboutToSubmit() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);

        final Response response = triggerCallback(caseData, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, ABOUT_TO_SUBMIT_URL, true);

        Long caseId = functionalTestDataManager.getTestReferences()
            .get(functionalTestDataManager.getTestReferences().size() - 1);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        Map<String, String> migratedDocBinarys = new HashMap<>();

        caseDocumentsFTDataManager.getDocumentEntities(caseId).forEach(documentEntity ->
            migratedDocBinarys.put(documentEntity.getDocumentTypeName(), documentEntity.getDocumentBinaryUrl()));

        assertThat(migratedDocBinarys).containsExactlyInAnyOrderEntriesOf(listOfTestBinarys);
    }
}
