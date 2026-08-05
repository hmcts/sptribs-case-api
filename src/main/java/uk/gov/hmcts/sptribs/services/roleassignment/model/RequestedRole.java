package uk.gov.hmcts.sptribs.services.roleassignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestedRole {
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String roleCategory;
    private String classification;
    private String grantType;
    private boolean readOnly;
    private Map<String, String> attributes;
}
