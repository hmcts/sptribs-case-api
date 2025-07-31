package uk.gov.hmcts.sptribs.services.wa;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.model.wa.InitiateTaskRequest;
import uk.gov.hmcts.sptribs.services.model.wa.ProcessCategoryIdentifier;
import uk.gov.hmcts.sptribs.services.model.wa.Search;
import uk.gov.hmcts.sptribs.services.model.wa.TaskType;
import uk.gov.hmcts.sptribs.services.model.wa.WaRequest;
import uk.gov.hmcts.sptribs.services.model.wa.WaTask;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.services.model.wa.Search.SearchOperator.IN;
import static uk.gov.hmcts.sptribs.services.model.wa.Search.SearchParameterKey.CASE_ID;


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
    public List<WaTask> searchTasks(WaRequest.SearchTaskRequest searchTaskRequest) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();

        log.info("Searching for tasks with request: {}", searchTaskRequest);
        return taskManagementClient.searchTask(user.getAuthToken(), serviceAuthToken, 0, 100, searchTaskRequest).getTasks();
    }

    public void initiateTask(String caseId, TaskType taskType, Map<String, Object> taskAttributes) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();
        InitiateTaskRequest initiateTaskRequest = InitiateTaskRequest.builder()
            .caseId(caseId)
            .taskType(taskType)
            .taskAttributes(taskAttributes)
            .build();
        try {
            log.info("Attempting to initiate task of type {} for case: {}", taskType, caseId);
            taskManagementClient.initiateTaskForCase(user.getAuthToken(), serviceAuthToken, caseId, initiateTaskRequest);
            log.info("Task {} initiated successfully for case: {}", taskType, caseId);
        } catch (FeignException e) {
            log.error("Error initiating tasks for case id: {}. Status: {}, Body: {}", caseId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to initiate task", e);
        }
    }

    /**
     * Reconfigures existing tasks with the lastest case data (usually Case Management Location Changes) that belong to the provided
     * process category identifiers.
     * @param caseId
     * @param processCategoryIdentifiers
     */
    public void reconfigureTask(String caseId, List<ProcessCategoryIdentifier> processCategoryIdentifiers) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();
        try {
            log.info("Attempting to reconfigure tasks for case: {}", caseId);
            taskManagementClient.reconfigureTask(user.getAuthToken(), serviceAuthToken, caseId, processCategoryIdentifiers);
            log.info("Task reconfigured successfully for case: {}", caseId);
        } catch (FeignException e) {
            log.error("Error reconfiguring tasks for case id: {}. Status: {}, Body: {}", caseId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to reconfigure task", e);
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
     * Cancels all tasks for a specific case which also belongs to the provided process category id - these ids are service-defined.
     *
     * @param caseId The CCD Case ID.
     * @param processCategoryIdentifier The process category identifier to cancel tasks for.
     */
    public void cancelTasksForProcessCategoryId(String caseId,
                                                ProcessCategoryIdentifier processCategoryIdentifier) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();
        try {
            log.info("Attempting to cancel tasks for case id: {}", caseId);
            taskManagementClient.cancelForCase(user.getAuthToken(), serviceAuthToken, caseId, List.of(processCategoryIdentifier));
            log.info("Tasks cancelled successfully for caseId: {}", caseId);
        } catch (FeignException e) {
            log.error("Error cancelling task for caseId: {}. Status: {}, Body: {}", caseId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to cancel task", e);
        }

    }

    /**
     * Completes tasks for a specific case ID. Often, a user event triggers this action and auto-completes a number of task types at once.
     * @param caseId
     * @param taskTypes
     */
    public void completeTasks(String caseId, TaskType... taskTypes) {
        User user = idamService.retrieveSystemUpdateUserDetails();
        String serviceAuthToken = authTokenGenerator.generate();

        try {
            log.info("Attempting to complete tasks for case id: {}", caseId);
            taskManagementClient.completeTasks(user.getAuthToken(), serviceAuthToken, caseId, List.of(taskTypes));
            log.info("Tasks completed successfully for caseId: {}", caseId);
        } catch (FeignException e) {
            log.error("Error completing tasks for caseId: {}. Status: {}, Body: {}", caseId, e.status(), e.contentUTF8(), e);
            throw new CaseCreateOrUpdateException("Failed to cancel task", e);
        }
    }

    /**
     * Convenience method to search for all tasks related to a specific case ID.
     * @param caseId The CCD Case ID.
     * @return A list of tasks for the given case.
     */
    public List<WaTask> getTasksForCase(String caseId) {
        WaRequest.SearchTaskRequest searchTaskRequest = WaRequest.SearchTaskRequest.builder()
            .searchParameters(List.of(
                new Search.SearchParameterList(CASE_ID, IN, List.of(caseId))
            ))
            .build();
        return searchTasks(searchTaskRequest);
    }
}
