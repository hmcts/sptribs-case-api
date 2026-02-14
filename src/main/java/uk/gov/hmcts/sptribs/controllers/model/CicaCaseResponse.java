package uk.gov.hmcts.sptribs.controllers.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CicaCaseResponse {

    private String id;

    private String state;

    private Map<String, JsonNode> data;
}




