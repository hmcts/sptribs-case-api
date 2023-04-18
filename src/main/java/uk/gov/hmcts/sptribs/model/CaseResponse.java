package uk.gov.hmcts.sptribs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CaseResponse {
    private String status;
    private Long id;
    private Map<String, Object> caseData;
}
