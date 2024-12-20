package uk.gov.hmcts.sptribs.testutil;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.concurrent.TimeUnit;

@TestPropertySource("classpath:application.yaml")
@Service
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Value("${idam.caseworker.username}")
    private String caseworkerUsername;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    @Value("${idam.waseniorcaseworker.username}")
    private String waSeniorCaseworkerUsername;

    @Value("${idam.waseniorcaseworker.password}")
    private String waSeniorCaseworkerPassword;

    @Value("${idam.waregionalhearingcentreteamlead.username}")
    private String waRegionalHearingCentreTeamLeadUsername;

    @Value("${idam.waregionalhearingcentreteamlead.password}")
    private String waRegionalHearingCentreTeamLeadPassword;

    @Value("${idam.systemupdate.username}")
    private String systemUpdateUsername;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Value("${idam.citizen.username}")
    private String citizenUsername;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Autowired
    private IdamClient idamClient;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    public String generateIdamTokenForSolicitor() {
        String solicitorUserToken = cache.getIfPresent(solicitorUsername);
        if (solicitorUserToken == null) {
            solicitorUserToken = idamClient.getAccessToken(solicitorUsername, solicitorPassword);
            cache.put(solicitorUsername, solicitorUserToken);
        }
        return solicitorUserToken;
    }

    public String generateIdamTokenForCaseworker() {
        String caseworkerUserToken = cache.getIfPresent(caseworkerUsername);
        if (caseworkerUserToken == null) {
            caseworkerUserToken = idamClient.getAccessToken(caseworkerUsername, caseworkerPassword);
            cache.put(caseworkerUsername, caseworkerUserToken);
        }
        return caseworkerUserToken;
    }

    public String generateIdamTokenForWASeniorCaseworker() {
        String caseworkerUserToken = cache.getIfPresent(waSeniorCaseworkerUsername);
        if (caseworkerUserToken == null) {
            caseworkerUserToken = idamClient.getAccessToken(waSeniorCaseworkerUsername, waSeniorCaseworkerPassword);
            cache.put(waSeniorCaseworkerUsername, caseworkerUserToken);
        }
        return caseworkerUserToken;
    }

    public String generateIdamTokenForWARegionalHearingCentreTeamLead() {
        String adminUserToken = cache.getIfPresent(waRegionalHearingCentreTeamLeadUsername);
        if (adminUserToken == null) {
            adminUserToken = idamClient.getAccessToken(waRegionalHearingCentreTeamLeadUsername, waRegionalHearingCentreTeamLeadPassword);
            cache.put(waRegionalHearingCentreTeamLeadUsername, adminUserToken);
        }
        return adminUserToken;
    }

    public String generateIdamTokenForSystemUser() {
        String systemUserToken = cache.getIfPresent(systemUpdateUsername);
        if (systemUserToken == null) {
            systemUserToken = idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
            cache.put(systemUpdateUsername, systemUserToken);
        }
        return systemUserToken;
    }

    public String generateIdamTokenForCitizen() {
        String citizenUserToken = cache.getIfPresent(citizenUsername);
        if (citizenUserToken == null) {
            citizenUserToken = idamClient.getAccessToken(citizenUsername, citizenPassword);
            cache.put(citizenUsername, citizenUserToken);
        }
        return citizenUserToken;
    }

    public String generateIdamTokenForUser(String username, String password) {
        String userToken = cache.getIfPresent(username);
        if (userToken == null) {
            userToken = idamClient.getAccessToken(username, password);
            cache.put(username, userToken);
        }
        return userToken;
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
