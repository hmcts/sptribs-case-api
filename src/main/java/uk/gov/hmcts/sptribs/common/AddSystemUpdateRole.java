package uk.gov.hmcts.sptribs.common;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEMUPDATE;

@Component
public class AddSystemUpdateRole {

    private static final String ENVIRONMENT_AAT = "aat";

    public List<UserRole> addIfConfiguredForEnvironment(List<UserRole> userRoles) {
        List<UserRole> existingRoles = new ArrayList<>(userRoles);

        if (isEnvironmentAat()) {
            existingRoles.add(SYSTEMUPDATE);
        }

        return existingRoles;
    }

    private boolean isEnvironmentAat() {
        final String currentEnv = System.getenv().getOrDefault("ENVIRONMENT", null);
        return null != currentEnv && currentEnv.equalsIgnoreCase(ENVIRONMENT_AAT);
    }
}
