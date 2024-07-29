package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.List;

public class Expectation {

    private String credentials;
    private final Integer status;
    private final Integer numberOfTasksAvailable;
    private final Task taskData;
    private final List<RoleData> roleData;

    public Expectation(Integer status, Integer numberOfTasksAvailable, Task taskData, List<RoleData> roleData) {
        this.status = status;
        this.numberOfTasksAvailable = numberOfTasksAvailable;
        this.taskData = taskData;
        this.roleData = roleData;
    }
}
