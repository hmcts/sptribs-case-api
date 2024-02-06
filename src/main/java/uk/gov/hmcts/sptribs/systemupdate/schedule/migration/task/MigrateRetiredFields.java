package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RetiredFields;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.util.Map;

@Component
@Slf4j
public class MigrateRetiredFields implements CaseTask {

    private final ObjectMapper objectMapper;

    @Autowired
    public MigrateRetiredFields(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) throws IllegalArgumentException  {

        final Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        final Map<String, Object> migratedMappedData = RetiredFields.migrate(mappedData);
        final CaseData convertedData = objectMapper.convertValue(migratedMappedData, CaseData.class);

        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
