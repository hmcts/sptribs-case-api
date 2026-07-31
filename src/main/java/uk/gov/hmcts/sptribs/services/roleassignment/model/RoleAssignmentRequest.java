package uk.gov.hmcts.sptribs.services.roleassignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentRequest {
    private RoleRequest roleRequest;
    private List<RequestedRole> requestedRoles;
}
