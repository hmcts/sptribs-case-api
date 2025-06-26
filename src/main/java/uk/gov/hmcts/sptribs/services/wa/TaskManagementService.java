package uk.gov.hmcts.sptribs.services.wa;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Domain.Task;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Request.InitiateTaskRequestMap;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Request.Options.TerminateInfo;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Request.SearchTaskRequest;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Request.TerminateTaskRequest;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Domain.Search.SearchOperator.IN;
import static uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Domain.Search.SearchParameterKey.CASE_ID;
import static uk.gov.hmcts.sptribs.services.wa.TaskManagementClient.Request.Enums.InitiateTaskOperation.INITIATION;


@Service
@Slf4j
@RequiredArgsConstructor
public class TaskManagementService {

    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final TaskManagementClient taskManagementClient;

    /**
     * Searches for tasks based on a search request.
     * @param searchTaskRequest The request containing search parameters.
     * @return A list of tasks matching the criteria.
     */
    public List<Task> searchTasks(SearchTaskRequest searchTaskRequest) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();

        log.info("Searching for tasks with request: {}", searchTaskRequest);
        return taskManagementClient.searchTask(user.getAuthToken(), serviceAuthToken, 0, 100, searchTaskRequest).getTasks();
    }

    /**
     * Initiates a task in the WA system.
     * This is usually called after a task is created in Camunda.
     *
     * @param taskId         The unique ID of the task to initiate.
     * @param taskAttributes A map of attributes to set on the task.
     */
    public void initiateTask(String taskId, Map<String, Object> taskAttributes) {
        String serviceAuthToken = authTokenGenerator.generate();
        InitiateTaskRequestMap initiateTaskRequest = new InitiateTaskRequestMap(INITIATION, taskAttributes);
        try {
            log.info("Attempting to initiate task with id: {}", taskId);
            taskManagementClient.initiateTask(serviceAuthToken, taskId, initiateTaskRequest);
            log.info("Task initiated successfully: {}", taskId);
        } catch (FeignException e) {
            log.error("Error initiating task id: {}. Status: {}, Body: {}", taskId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to initiate task", e);
        }
    }

    /**
     * Cancels a task. This is typically used for user-driven cancellations.
     * Requires both a user token and a service token.
     *
     * @param taskId The unique ID of the task to cancel.
     */
    public void cancelTask(String taskId) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();
        try {
            log.info("Attempting to cancel task with id: {}", taskId);
            taskManagementClient.cancelTask(user.getAuthToken(), serviceAuthToken, taskId);
            log.info("Task cancelled successfully: {}", taskId);
        } catch (FeignException e) {
            log.error("Error cancelling task id: {}. Status: {}, Body: {}", taskId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to cancel task", e);
        }
    }

    /**
     * Terminates a task. This is a more definitive system-level action.
     *
     * @param taskId          The unique ID of the task to terminate.
     * @param terminateReason A string explaining why the task is being terminated.
     */
    public void terminateTask(String taskId, String terminateReason) {
        String serviceAuthToken = authTokenGenerator.generate();
        TerminateTaskRequest terminateTaskRequest = new TerminateTaskRequest(new TerminateInfo(terminateReason));
        try {
            log.info("Attempting to terminate task with id: {} for reason: {}", taskId, terminateReason);
            taskManagementClient.terminateTask(serviceAuthToken, taskId, terminateTaskRequest);
            log.info("Task terminated successfully: {}", taskId);
        } catch (FeignException e) {
            log.error("Error terminating task id: {}. Status: {}, Body: {}", taskId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to terminate task", e);
        }
    }

    /**
     * Convenience method to search for all tasks related to a specific case ID.
     * @param caseId The CCD Case ID.
     * @return A list of tasks for the given case.
     */
    public List<Task> getTasksForCase(String caseId) {
        SearchTaskRequest searchTaskRequest = SearchTaskRequest.builder()
            .searchParameters(List.of(
                new TaskManagementClient.Domain.Search.SearchParameterList(CASE_ID, IN, List.of(caseId))
            ))
            .build();
        return searchTasks(searchTaskRequest);
    }
}
