package uk.gov.hmcts.sptribs.services.roleassignment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.sptribs.services.roleassignment.model.RoleAssignmentRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "role-assignment-api",
    url = "${role-assignment.url}"
)
public interface RoleAssignmentApi {

    @PostMapping(
        value = "/am/role-assignments",
        consumes = APPLICATION_JSON_VALUE
    )
    void assignRole(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody RoleAssignmentRequest roleAssignmentRequest
    );
}
