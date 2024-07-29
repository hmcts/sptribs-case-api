package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

import java.util.ArrayList;

public class Expectation {

    private String credentials;
    private final Integer status;
    private final Integer numberOfTasksAvailable;
    private final Task taskData;
    private final ArrayList<RoleData> roleData;

    public Expectation(Integer status, Integer numberOfTasksAvailable, Task taskData, ArrayList<RoleData> roleData) {
        this.status = status;
        this.numberOfTasksAvailable = numberOfTasksAvailable;
        this.taskData = taskData;
        this.roleData = roleData;
    }
}
