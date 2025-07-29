package uk.gov.hmcts.sptribs.services.wa;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.sptribs.services.model.wa.ProcessCategoryIdentifier;
import uk.gov.hmcts.sptribs.services.model.wa.TaskType;
import uk.gov.hmcts.sptribs.services.model.wa.WaRequest;
import uk.gov.hmcts.sptribs.services.model.wa.WaResponse;
import uk.gov.hmcts.sptribs.services.model.wa.WaTask;
import uk.gov.hmcts.sptribs.services.wa.config.WaFeignClientSnakeCaseConfiguration;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Feign client for the Work Allocation Task Management API.
 * Includes all required DTOs as nested static classes for convenience.
 */
@FeignClient(
    name = "wa-task-management-api",
    url = "${wa-task-management-api.url}",
    configuration = WaFeignClientSnakeCaseConfiguration.class
)
public interface TaskManagementClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String AUTHORIZATION = "Authorization";

    @PostMapping(path = "/task", consumes = APPLICATION_JSON_VALUE)
    WaResponse.GetTasksResponse<WaTask> searchTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @RequestParam(name = "first_result", required = false, defaultValue = "0") int firstResult,
        @RequestParam(name = "max_results", required = false, defaultValue = "50") int maxResults,
        @RequestBody WaRequest.SearchTaskRequest searchTaskRequest
    );

    @GetMapping(path = "/task/{task-id}")
    WaResponse.GetTaskResponse<WaTask> getTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/claim")
    ResponseEntity<Void> claimTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/unclaim")
    ResponseEntity<Void> unclaimTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{task-id}/assign")
    ResponseEntity<Void> assignTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody WaRequest.AssignTaskRequest assignTaskRequest
    );

    @PostMapping(path = "/task/{task-id}/complete")
    ResponseEntity<Void> completeTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody(required = false) WaRequest.CompleteTaskRequest completeTaskRequest
    );

    @PostMapping(path = "/task/{case-id}/complete")
    ResponseEntity<Void> completeTasks(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("case-id") String caseId,
        @RequestBody List<TaskType> taskTypes
    );

    @PostMapping(path = "/task/{task-id}/cancel")
    ResponseEntity<Void> cancelTask(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId
    );

    @PostMapping(path = "/task/{case-id}/cancel-process-category")
    ResponseEntity<Void> cancelForCase(
        @RequestHeader(AUTHORIZATION) String authToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("case-id") String caseId,
        @RequestBody List<ProcessCategoryIdentifier> processCategoryIdentifiers
    );

    @PostMapping(path = "/task/{task-id}/initiation", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<WaTask> initiateTask(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody WaRequest.InitiateTaskRequestMap initiateTaskRequest
    );

    @DeleteMapping(path = "/task/{task-id}")
    ResponseEntity<Void> terminateTask(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthToken,
        @PathVariable("task-id") String taskId,
        @RequestBody WaRequest.TerminateTaskRequest terminateTaskRequest
    );


}
