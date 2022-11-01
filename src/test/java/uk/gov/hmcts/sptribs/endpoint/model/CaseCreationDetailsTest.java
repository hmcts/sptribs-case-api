package uk.gov.hmcts.sptribs.endpoint.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseCreationDetailsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldCaseCreationDetailsJsonMapping() throws JsonProcessingException {
        //Given
        String json = "{\"case_type_id\":\"CIC\", \"event_id\":\"1\", \"case_data\": {\"case_id\":1}}";
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("case_id", 1);
        CaseCreationDetails constructedCaseCreationDetails = new CaseCreationDetails("CIC", "1", caseData);

        //When
        CaseCreationDetails caseCreationDetails = mapper.readValue(json, CaseCreationDetails.class);

        //Then
        assertThat(caseCreationDetails.getCaseTypeId()).isEqualTo(constructedCaseCreationDetails.getCaseTypeId());
        assertThat(caseCreationDetails.getEventId()).isEqualTo(constructedCaseCreationDetails.getEventId());
        assertThat(caseCreationDetails.getCaseData()).isEqualTo(constructedCaseCreationDetails.getCaseData());
    }

}
