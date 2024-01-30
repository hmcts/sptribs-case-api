package uk.gov.hmcts.sptribs.idam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Service
public class IdamService {

    private final String systemUpdateUserName;

    private final String systemUpdatePassword;

    private final IdamClient idamClient;

    @Autowired
    public IdamService(
        @Value("${idam.systemupdate.username}") String systemUpdateUserName,
        @Value("${idam.systemupdate.password}") String systemUpdatePassword,
        IdamClient idamClient) {
        this.systemUpdateUserName = systemUpdateUserName;
        this.systemUpdatePassword = systemUpdatePassword;
        this.idamClient = idamClient;
    }

    public User retrieveUser(String authorisation) {
        final String bearerToken = getBearerToken(authorisation);
        final UserDetails userDetails = idamClient.getUserDetails(bearerToken);

        return new User(bearerToken, userDetails);
    }

    public User retrieveSystemUpdateUserDetails() {
        return retrieveUser(getIdamOauth2Token(systemUpdateUserName, systemUpdatePassword));
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    private String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }
        return token.startsWith(BEARER_PREFIX) ? token : BEARER_PREFIX.concat(token);
    }
}
