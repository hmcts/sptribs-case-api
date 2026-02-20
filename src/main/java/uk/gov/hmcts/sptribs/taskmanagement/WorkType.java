package uk.gov.hmcts.sptribs.taskmanagement;

public enum WorkType {
    ROUTINE_WORK,
    PRIORITY,
    HEARING_WORK,
    DECISION_MAKING_WORK,
    APPLICATIONS;

    public String getLowerCaseName() {
        return name().toLowerCase();
    }
}




