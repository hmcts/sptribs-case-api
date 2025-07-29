package uk.gov.hmcts.sptribs.services.model.wa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient;

import java.util.Set;

@Getter @AllArgsConstructor
public class TaskPermissions {
    private Set<PermissionTypes> values;
}
