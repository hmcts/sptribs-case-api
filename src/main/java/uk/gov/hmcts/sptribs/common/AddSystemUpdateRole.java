package uk.gov.hmcts.sptribs.common;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC.SYSTEMUPDATE;

@Component
public class AddSystemUpdateRole {

    private static final String ENVIRONMENT_AAT = "aat";

    public List<UserRoleCIC> addIfConfiguredForEnvironment(List<UserRoleCIC> userRoleCICS) {
        List<UserRoleCIC> existingRoles = new ArrayList<>(userRoleCICS);
        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);

        if (null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT)) {
            existingRoles.add(SYSTEMUPDATE);
        }

        return existingRoles;
    }

    public boolean isEnvironmentAat() {
        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);
        return null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT);
    }
}
