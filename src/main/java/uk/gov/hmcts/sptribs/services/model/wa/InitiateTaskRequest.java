package uk.gov.hmcts.sptribs.services.model.wa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InitiateTaskRequest {
    private String caseId;
    private TaskType taskType;
    private Map<String, Object> taskAttributes;
}
