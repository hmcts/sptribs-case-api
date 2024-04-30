package uk.gov.hmcts.sptribs.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;


@Component
public class CaseDetailsConverter {

    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> convertToCaseDetailsFromReformModel(
        final CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });
    }
}
