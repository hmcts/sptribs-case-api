package uk.gov.hmcts.sptribs.wa;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.testutil.CcdCaseCreator;
import uk.gov.hmcts.sptribs.testutil.CdamUrlDebugger;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;
import uk.gov.hmcts.sptribs.testutil.RoleAssignmentService;
import uk.gov.hmcts.sptribs.testutil.TaskManagementService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_BUILT;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CLOSE_THE_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_CASE;

@SpringBootTest
@Slf4j
public class WAProcessCorrectionsFT extends FunctionalTestSuite {
    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    @Autowired
    private CdamUrlDebugger cdamUrlDebugger;

    private static final String TASK_TYPE = "processCorrections";
    private static final List<String> TASK_ROLES = Arrays.asList("regional-centre-admin", "regional-centre-team-leader", "task-supervisor",
        "hearing-centre-admin", "hearing-centre-team-leader", "ctsc", "ctsc-team-leader");
    private static final int DEFAULT_TIMEOUT_SECONDS = 300;
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 4;

    private static final String CASEWORKER_CREATE_DRAFT_ORDER_DATA = "classpath:wa/caseworker-create-draft-order-submit-data.json";
    private static final ClassPathResource DRAFT_ORDER_FILE =
            new ClassPathResource("files/DRAFT :Order--[Subject Name]--26-04-2024 10:09:12.pdf");

    @Test
    @EnabledIfEnvironmentVariable(named = "WA_FUNCTIONAL_TESTS_ENABLED", matches = "true")
    void shouldInitiateProcessCorrectionsTask() throws IOException {
        final Response response = createAndSubmitTestCaseAndGetResponse();
        final long id = response.getBody().path("id");
        final String newCaseId = String.valueOf(id);
        final Map<String, Object> caseData = response.getBody().path("caseData");

        log.debug("New case created: {}", newCaseId);

        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_EDIT_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(CASEWORKER_CASE_BUILT, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);
        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_CLOSE_THE_CASE, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);


        caseData.put("cicCaseReferralTypeForWA", "Corrections");
        caseData.putAll(caseData(CASEWORKER_CREATE_DRAFT_ORDER_DATA));
        cdamUrlDebugger.logUrls();
        UploadResponse uploadResponse = uploadTestDocument(DRAFT_ORDER_FILE);

        if (uploadResponse != null) {
            log.info("Document uploaded: {}", uploadResponse.getDocuments().getFirst());
            updateOrderTemplate(uploadResponse.getDocuments().getFirst(), caseData);
        }

        ccdCaseCreator.createInitialStartEventAndSubmit(
            CASEWORKER_CREATE_DRAFT_ORDER, ST_CIC_JURISDICTION, ST_CIC_CASE_TYPE, newCaseId, caseData);

        await()
            .pollInterval(DEFAULT_POLL_INTERVAL_SECONDS, SECONDS)
            .atMost(DEFAULT_TIMEOUT_SECONDS, SECONDS)
            .until(
                () -> {
                    roleAssignmentService.createRoleAssignmentsForWaSeniorCaseworker();

                    Response searchByCaseIdResponseBody =
                        taskManagementService.search(newCaseId, List.of(TASK_TYPE), 1, 200);

                    if (searchByCaseIdResponseBody.asString().isBlank()) {
                        return false;
                    }

                    final List<Map<String, Object>> tasks = searchByCaseIdResponseBody.getBody().path("tasks");
                    final String taskId = searchByCaseIdResponseBody.getBody().path("tasks[0].id");
                    final String taskType = searchByCaseIdResponseBody.getBody().path("tasks[0].type");

                    assertNotNull(tasks);
                    assertThat(tasks).isNotEmpty();
                    assertThat(taskType).isEqualTo(TASK_TYPE);

                    Response retrieveTaskRolePermissionsResponseBody =
                        taskManagementService.retrieveTaskRolePermissions(taskId, 7, 200);

                    if (retrieveTaskRolePermissionsResponseBody.asString().isBlank()) {
                        return false;
                    }

                    final List<Map<String, Object>> roles = retrieveTaskRolePermissionsResponseBody.getBody().path("roles");

                    assertNotNull(roles);
                    assertThat(roles).isNotEmpty();

                    for (Map<String, Object> role : roles) {
                        String roleName = role.get("role_name").toString();
                        assertThat(roleName).isIn(TASK_ROLES);
                    }

                    return true;
                });
    }

    @SuppressWarnings("unchecked")
    private void updateOrderTemplate(Document document, Map<String, Object> caseData) {
        Map<String, Object> orderTemplateIssued = (Map<String, Object>) caseData.get("cicCaseOrderTemplateIssued");
        if (orderTemplateIssued != null) {
            orderTemplateIssued.put("document_url", document.links.self.href);
            orderTemplateIssued.put("document_filename", document.originalDocumentName);
            orderTemplateIssued.put("document_binary_url", document.links.binary.href);
        }
        caseData.put("cicCaseOrderTemplateIssued", orderTemplateIssued);

        List<Map<String, Object>> draftOrderList = (List<Map<String, Object>>) caseData.get("cicCaseDraftOrderCICList");
        if (draftOrderList != null && !draftOrderList.isEmpty()) {
            Map<String, Object> firstItem = draftOrderList.getFirst();
            Map<String, Object> value = (Map<String, Object>) firstItem.get("value");
            if (value != null) {
                Map<String, Object> templateGeneratedDocument = (Map<String, Object>) value.get("templateGeneratedDocument");
                if (templateGeneratedDocument != null) {
                    templateGeneratedDocument.put("document_url", document.links.self.href);
                    templateGeneratedDocument.put("document_filename", document.originalDocumentName);
                    templateGeneratedDocument.put("document_binary_url", document.links.binary.href);
                }
                value.put("templateGeneratedDocument", templateGeneratedDocument);
            }
        }
        caseData.put("cicCaseDraftOrderCICList", draftOrderList);
    }
}
