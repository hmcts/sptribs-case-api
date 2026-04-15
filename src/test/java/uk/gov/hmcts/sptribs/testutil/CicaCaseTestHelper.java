package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

import java.util.Map;

public class CicaCaseTestHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CicaCaseEntity createCicaCaseEntity(String ccdReference) {
        JsonNode cicaNode = objectMapper.valueToTree("X1234");
        JsonNode fullNameNode = objectMapper.valueToTree("John Smith");
        return CicaCaseEntity.builder()
            .id(ccdReference)
            .state("Submitted")
            .data(Map.of(
                "cicCaseCicaReferenceNumber", cicaNode,
                "cicCaseFullName", fullNameNode
            ))
            .build();
    }

    public static CicaCaseResponse createCicaCaseResponse(String ccdReference) {
        JsonNode cicaNode = objectMapper.valueToTree("X1234");
        JsonNode fullNameNode = objectMapper.valueToTree("John Smith");
        return CicaCaseResponse.builder()
            .id(ccdReference)
            .state("Submitted")
            .data(Map.of(
                "cicCaseCicaReferenceNumber", cicaNode,
                "cicCaseFullName", fullNameNode
            ))
            .build();
    }
}
