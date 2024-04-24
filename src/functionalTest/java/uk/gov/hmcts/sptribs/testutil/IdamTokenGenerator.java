package uk.gov.hmcts.sptribs.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

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

    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }

    public String generateIdamTokenForCaseworker() {
        return idamClient.getAccessToken(caseworkerUsername, caseworkerPassword);
    }

    public String generateIdamTokenForSystem() {
        return idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
    }

    public String generateIdamTokenForUser(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    public String generateIdamTokenForCitizen() {
        return idamClient.getAccessToken(citizenUsername, citizenPassword);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
