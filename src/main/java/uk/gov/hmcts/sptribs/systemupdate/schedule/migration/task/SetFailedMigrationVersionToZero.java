package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.util.Map;

@Component
@Slf4j
public class SetFailedMigrationVersionToZero implements CaseTask {

    private final ObjectMapper objectMapper;

    public static final int HIGHEST_PRIORITY = 0;

    @Autowired
    public SetFailedMigrationVersionToZero(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        mappedData.put("dataVersion", HIGHEST_PRIORITY);

        final CaseData convertedData = objectMapper.convertValue(mappedData, CaseData.class);
        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
