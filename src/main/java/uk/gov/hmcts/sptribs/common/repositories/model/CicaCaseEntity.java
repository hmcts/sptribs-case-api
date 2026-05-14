package uk.gov.hmcts.sptribs.common.repositories.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class CicaCaseEntity {

    private String id;

    private String state;

    private Map<String, JsonNode> data;
}
