package uk.gov.hmcts.sptribs.services.roleassignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {
    private String assignerId;
    private String process;
    private String reference;
    private boolean replaceExisting;
}
