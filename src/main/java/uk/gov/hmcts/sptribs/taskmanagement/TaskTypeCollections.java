package uk.gov.hmcts.sptribs.taskmanagement;

import uk.gov.hmcts.sptribs.taskmanagement.model.TaskType;

import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.sptribs.taskmanagement.model.ProcessCategoryIdentifiers.IssueCase;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.followUpNoncomplianceOfDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.processFurtherEvidence;
import static uk.gov.hmcts.sptribs.taskmanagement.model.TaskType.reviewOrder;

public final class TaskTypeCollections {

    public static final List<TaskType> ISSUE_CASE_CANCELLABLE_TASKS =
        TaskType.getTaskTypesFromProcessCategoryIdentifiers(List.of(IssueCase));

    public static final List<TaskType> REFERRAL_COMPLETABLE_TASKS =
        List.of(followUpNoncomplianceOfDirections, processFurtherEvidence);

    public static final List<TaskType> REVIEW_TASKS_TO_COMPLETE = Stream
        .of(TaskType.values())
        .filter(taskType -> taskType.name().startsWith("review")
            && !taskType.name().startsWith("reviewSpecificAccess")
            && taskType != reviewOrder)
        .toList();

    private TaskTypeCollections() {
    }
}
