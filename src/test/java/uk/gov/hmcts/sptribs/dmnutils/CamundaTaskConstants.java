package uk.gov.hmcts.sptribs.dmnutils;

public class CamundaTaskConstants {

    private CamundaTaskConstants() {
    }

    public static final String CASE_NAME = "caseName";
    public static final String CASE_MANAGEMENT_CATEGORY = "caseManagementCategory";
    public static final String REGION = "region";
    public static final String LOCATION = "location";
    public static final String LOCATION_NAME = "locationName";
    public static final String DUE_DATE_NON_WORKING_CALENDAR = "dueDateNonWorkingCalendar";
    public static final String DUE_DATE_WORKING_DAYS_OF_WEEK = "dueDateNonWorkingDaysOfWeek";
    public static final String WORK_TYPE = "workType";
    public static final String ROLE_CATEGORY = "roleCategory";
    public static final String MINOR_PRIORITY = "minorPriority";
    public static final String MAJOR_PRIORITY = "majorPriority";
    public static final String DESCRIPTION = "description";
    public static final String DUE_DATE_INTERVAL_DAYS = "dueDateIntervalDays";
    public static final String DUE_DATE_ORIGIN = "dueDateOrigin";
    public static final String DUE_DATE_TIME = "dueDateTime";
    public static final String PRIORITY_DATE_ORIGIN_REF = "priorityDateOriginRef";
    public static final String ADDITIONAL_PROPERTIES_ROLE_ASSIGNMENT_ID = "additionalProperties_roleAssignmentId";

    public static final String DECISION_WORK_TYPE = "decision_making_work";
    public static final String ROUTINE_WORK_TYPE = "routine_work";
    public static final String HEARING_WORK_TYPE = "hearing_work";
    public static final String PRIORITY_WORK_TYPE = "priority";
    public static final String APPLICATION_WORK_TYPE = "applications";
    public static final String ACCESS_WORK_TYPE = "access_requests";

    public static final String ROLE_CATEGORY_ADMIN = "ADMIN";
    public static final String ROLE_CATEGORY_LO = "LEGAL_OPERATIONS";
    public static final String ROLE_CATEGORY_JUDICIAL = "JUDICIAL";
    public static final String ROLE_CATEGORY_CTSC = "CTSC";

    public static final String DEFAULT_MINOR_PRIORITY = "500";
    public static final String DEFAULT_MAJOR_PRIORITY = "5000";
    public static final String URGENT_MAJOR_PRIORITY = "2000";
    public static final String DEFAULT_CASE_MANAGEMENT_CATEGORY = "Criminal Injuries Compensation";
    public static final String DEFAULT_REGION = "1";
    public static final String DEFAULT_LOCATION = "336559";
    public static final String DEFAULT_LOCATION_NAME = "Glasgow Tribunals Centre";
    public static final String DEFAULT_DUE_DATE_NON_WORKING_CALENDAR
        = "https://www.gov.uk/bank-holidays/scotland.json, https://raw.githubusercontent.com/hmcts/sptribs-case-api/master/src/main/resources/dmn/privilege-calendar.json";
    public static final String DEFAULT_DUE_DATE_WORKING_DAYS_OF_WEEK = "SATURDAY,SUNDAY";

    public static final String REVIEW_SPECIFIC_ACCESS_REQ_JUDICIARY_TASK = "reviewSpecificAccessRequestJudiciary";
    public static final String REVIEW_SPECIFIC_ACCESS_REQ_LO_TASK = "reviewSpecificAccessRequestLegalOps";
    public static final String REVIEW_SPECIFIC_ACCESS_REQ_ADMIN_TASK = "reviewSpecificAccessRequestAdmin";
    public static final String REVIEW_SPECIFIC_ACCESS_REQ_CTSC_TASK = "reviewSpecificAccessRequestCTSC";
}
