package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;

import java.util.List;

public final class SecurityUtil {
    private static final String[] PRIVATE_ROLES = {"senior-judge", "regional-center-team-leader", "senior-tribunal-caseworker"};
    private static final String[] RESTRICTED_ROLES = {"senior-judge"};
    private static final String[] PUBLIC_ROLES = {"hmcts-judiciary"};

    private SecurityUtil() {

    }

    public static boolean checkAvailableForNewClass(User user, SecurityClass newClass) {
        UserDetails userDetails = user.getUserDetails();
        List<String> roles = userDetails.getRoles();
        if (null != newClass && newClass.equals(SecurityClass.PRIVATE)) {
            for (String privateRole : PRIVATE_ROLES) {
                if (roles.contains(privateRole)) {
                    return true;
                }
            }
        } else if (null != newClass && newClass.equals(SecurityClass.RESTRICTED)) {
            for (String role : RESTRICTED_ROLES) {
                if (roles.contains(role)) {
                    return true;
                }
            }
        } else {
            for (String role : PUBLIC_ROLES) {
                if (roles.contains(role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
