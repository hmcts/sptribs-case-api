package uk.gov.hmcts.sptribs.services.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaseDetailsConverterCopy {

    private final ObjectMapper objectMapper;

    public CaseDetailsConverterCopy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public CaseData toCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());

        return objectMapper.convertValue(data, CaseData.class);
    }

    public CaseData toCaseData(Map<String, Object> caseDataMap) {
        return objectMapper.convertValue(caseDataMap, CaseData.class);
    }

    public CaseData toGACaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());

        return objectMapper.convertValue(data, CaseData.class);
    }

}
