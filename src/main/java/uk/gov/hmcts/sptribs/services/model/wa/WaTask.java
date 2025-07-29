package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaTask {
    private String id;
    private String name;
    private String type;
    private String taskState;
    private String taskSystem;
    private String securityClassification;
    private String taskTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
    private ZonedDateTime createdDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
    private ZonedDateTime dueDate;
    private String assignee;
    private boolean autoAssigned;
    private String executionType;
    private String jurisdiction;
    private String region;
    private String location;
    private String locationName;
    private String caseTypeId;
    private String caseId;
    private String caseCategory;
    private String caseName;
    private Boolean warnings;
    private WarningValues warningList;
    private String caseManagementCategory;
    private String workTypeId;
    private TaskPermissions permissions;
    private String roleCategory;
    private String description;
    private Map<String, String> additionalProperties;
    private String nextHearingId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
    private ZonedDateTime nextHearingDate;
    private Integer minorPriority;
    private Integer majorPriority;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
    private ZonedDateTime priorityDate;
}

